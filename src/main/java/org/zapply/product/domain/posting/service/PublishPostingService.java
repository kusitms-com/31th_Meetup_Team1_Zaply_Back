package org.zapply.product.domain.posting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zapply.product.domain.posting.dto.request.PostingRequest;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.enumerate.MediaType;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.domain.posting.repository.PostingRepository;
import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.domain.project.repository.ProjectRepository;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.global.scheduler.service.SchedulingService;
import org.zapply.product.global.snsClients.instagram.InstagramPostingClient;
import org.zapply.product.global.snsClients.threads.ThreadsPostingClient;
import org.zapply.product.global.snsClients.threads.ThreadsPostingResponse;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublishPostingService {

    private final ThreadsPostingClient threadsPostingClient;
    private final ProjectRepository projectRepository;
    private final ImageService imageService;
    private final PostingRepository postingRepository;
    private final SchedulingService schedulingService;
    private final InstagramPostingClient instagramPostingClient;
    private final FacebookPostingService facebookPostingService;

    // 스레드 미디어 단일 발행하기
    public void publishSingleMediaNow(Member member, PostingRequest request, Long projectId) {
        threadsPostingClient.createSingleMedia(member, request, projectId);
    }

    // 미디어 단일 예약 발행하기
    @Transactional
    public void scheduleSingleMediaPublish(PostingRequest request, Long projectId, SNSType snsType) {
        // 프로젝트 검증
        Project project = projectRepository
                .findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.PROJECT_NOT_FOUND));

        // Posting 엔티티 생성 및 저장
        Posting posting = Posting.builder()
                .project(project)
                .postingType(snsType)
                .title(request.title())
                .postingContent(request.text())
                .scheduledAt(request.scheduledAt())
                .postingState(PostingState.SCHEDULED)
                .build();

        postingRepository.save(posting);
        imageService.saveAllImages(posting, request.media());

        schedulingService.scheduleTask(
                posting.getPostingId(),
                request.scheduledAt(),
                () -> executeScheduledSingleMedia(posting.getPostingId(), snsType)
        );
    }

    @Transactional
    public void rescheduleSingleMedia(Long postingId, LocalDateTime newScheduledAt, SNSType snsType, String content) {
        Posting posting = postingRepository.findByPostingIdAndPostingStateAndDeletedAtIsNull(postingId, PostingState.SCHEDULED)
                .orElseThrow(() -> new CoreException(GlobalErrorType.POSTING_NOT_FOUND));

        // 기존 일정 삭제 및 재등록
        schedulingService.cancelTask(postingId);
        posting.updateScheduledAt(newScheduledAt);
        posting.updatePostingContent(content);

        schedulingService.scheduleTask(
                postingId,
                newScheduledAt,
                () -> executeScheduledSingleMedia(postingId,snsType)
        );
    }

    @Transactional
    public void executeScheduledSingleMedia(Long postingId, SNSType snsType) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.POSTING_NOT_FOUND));

        List<String> imageUrls = imageService.getImagesURLByPosting(posting);
        Member member = posting.getProject().getMember();
        PostingRequest postingRequest = PostingRequest.of(MediaType.IMAGE, imageUrls, posting);

        String mediaId;

        switch (snsType) {
            case SNSType.THREADS ->
                    mediaId = threadsPostingClient.createSingleMedia(member, postingRequest, posting.getProject().getProjectId()).mediaId();
            case SNSType.INSTAGRAM ->
                    mediaId = instagramPostingClient.createSingleMedia(member, postingRequest, posting.getProject().getProjectId()).mediaId();
            case SNSType.FACEBOOK ->
                    mediaId = facebookPostingService.publishPostWithSinglePhotoAndText(member, postingRequest.text(), postingRequest.media().getFirst());
            default ->
                    throw new CoreException(GlobalErrorType.SNS_TYPE_NOT_FOUND);
        }

        posting.updatePostingState(PostingState.POSTED);
        posting.updateMediaId(mediaId);
    }

    // 스레드 미디어 캐러셀(다중) 발행하기
    public ThreadsPostingResponse publishCarouselMediaNow(Member member, PostingRequest request, Long projectId) {
        return threadsPostingClient.createCarouselMedia(member, request, projectId);
    }

    @Transactional
    public void scheduleCarouselMediaPublish(PostingRequest request, Long projectId, SNSType snsType) {
        Project project = projectRepository.findByProjectIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.PROJECT_NOT_FOUND));

        Posting posting = Posting.builder()
                .project(project)
                .postingType(snsType)
                .title(request.title())
                .postingContent(request.text())
                .scheduledAt(request.scheduledAt())
                .postingState(PostingState.SCHEDULED)
                .build();

        postingRepository.save(posting);
        imageService.saveAllImages(posting, request.media());

        schedulingService.scheduleTask(
                posting.getPostingId(),
                request.scheduledAt(),
                () -> executeScheduledCarouselMedia(posting.getPostingId(), snsType)
        );
    }

    @Transactional
    public void rescheduleCarouselMedia(Long postingId, LocalDateTime newScheduledAt, SNSType snsType, String content) {
        Posting posting = postingRepository
                .findByPostingIdAndPostingStateAndDeletedAtIsNull(postingId, PostingState.SCHEDULED)
                .orElseThrow(() -> new CoreException(GlobalErrorType.POSTING_NOT_FOUND));

        schedulingService.cancelTask(postingId);
        posting.updateScheduledAt(newScheduledAt);
        posting.updatePostingContent(content);

        schedulingService.scheduleTask(
                postingId,
                newScheduledAt,
                () -> executeScheduledCarouselMedia(postingId, snsType)
        );
    }

    /**
     * 예약된 캐러셀 미디어 게시 실행
     *
     * @param postingId 게시물 ID
     * @param snsType   SNS 타입
     */
    @Transactional
    public void executeScheduledCarouselMedia(Long postingId, SNSType snsType) {
        Posting posting = postingRepository.findById(postingId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.POSTING_NOT_FOUND));

        List<String> mediaUrls = imageService.getImagesURLByPosting(posting);
        Member member = posting.getProject().getMember();
        PostingRequest postingRequest = PostingRequest.of(MediaType.IMAGE, mediaUrls, posting);

        String mediaId;

        switch (snsType) {
            case SNSType.THREADS ->
                    mediaId = threadsPostingClient.createUpdatedCarouselMedia(member, postingRequest);
            case SNSType.INSTAGRAM ->
                    mediaId = instagramPostingClient.createCarouselMedia(member, postingRequest, posting.getProject().getProjectId()).mediaId();
            case SNSType.FACEBOOK ->
                    mediaId = facebookPostingService.publishPostWithMultiPhotoAndText(member, postingRequest.text(),postingRequest.media());
            default ->
                    throw new CoreException(GlobalErrorType.SNS_TYPE_NOT_FOUND);
        }
        posting.updatePostingState(PostingState.POSTED);
        posting.updateMediaId(mediaId);
    }
}
