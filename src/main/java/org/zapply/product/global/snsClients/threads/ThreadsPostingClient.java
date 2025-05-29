package org.zapply.product.global.snsClients.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.posting.dto.request.PostingRequest;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.domain.posting.repository.PostingRepository;
import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.domain.project.repository.ProjectRepository;
import org.zapply.product.domain.user.entity.Account;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.domain.user.repository.AccountRepository;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadsPostingClient {

    private static final String THREADS_API_BASE = "https://graph.threads.net/v1.0";

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final ProjectRepository projectRepository;
    private final PostingRepository postingRepository;

    private final RestClient restClient = RestClient.create();

    /**
     * Threads 계정 불러오기
     * @param member
     * @return account
     */
    private Account getThreadsAccount(Member member) {
        return accountRepository.findByAccountTypeAndMember(SNSType.THREADS, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_NOT_FOUND));
    }

    /**
     * 스레드 API에 요청하기 위한 액세스 토큰 가져오기
     * @param member
     * @return accessToken
     */
    private String getAccessToken(Member member) {
        return accountService.getAccessToken(member, SNSType.THREADS);
    }

    /**
     * 스레드 API에 POST 요청하기
     * @param uri
     * @return response
     */
    private Map<String, Object> postToThreadsApi(URI uri) {
        try {
            return restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("Error posting to Threads API: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }

    /**
     * 스레드 미디어 컨테이너 생성하기
     * @param mediaType
     * @param mediaUrl
     * @param accessToken
     * @param userId
     * @param isCarouselItem
     * @param text
     * @return mediaId
     */
    private String createMediaContainer(String mediaType, String mediaUrl, String accessToken, String userId, boolean isCarouselItem, String text) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(THREADS_API_BASE + "/" + userId + "/threads")
                .queryParam("media_type", mediaType)
                .queryParam("image_url", mediaUrl)
                .queryParam("access_token", accessToken);

        System.out.println("builder: " + builder.toUriString());
        if (isCarouselItem) {
            builder.queryParam("is_carousel_item", "true");
        } else if (text != null) {
            builder.queryParam("text", text);
        }
        URI uri = builder.build().encode().toUri();
        return String.valueOf(postToThreadsApi(uri).get("id"));
    }

    /**
     * 스레드 미디어 컨테이너 게시하기
     * @param creationId
     * @param accessToken
     * @param userId
     * @return publishedId
     */
    private String publishMediaContainer(String creationId, String accessToken, String userId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(THREADS_API_BASE + "/" + userId + "/threads_publish")
                .queryParam("creation_id", creationId)
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUri();

        return String.valueOf(postToThreadsApi(uri).get("id"));
    }

    /**
     * 스레드 게시물 저장하기
     * @param mediaId
     * @param projectId
     * @param mediaType
     * @param media
     * @param text
     * @return ThreadsPostingResponse
     */
    private ThreadsPostingResponse savePosting(String mediaId, Long projectId, String mediaType, String media, String text) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.PROJECT_NOT_FOUND));
        Posting posting = Posting.builder()
                .postingLink("")
                .postingType(SNSType.THREADS)
                .postingState(PostingState.POSTED)
                .project(project)
                .mediaId(mediaId)
                .build();
        postingRepository.save(posting);
        return ThreadsPostingResponse.of(posting, mediaType, media, text);
    }

    /**
     * 스레드 미디어 단일 게시물 생성하기
     * @param member
     * @param request
     * @param projectId
     * @return ThreadsPostingResponse
     */
    public ThreadsPostingResponse createSingleMedia(Member member, PostingRequest request, Long projectId) {
        try {
            Account account = getThreadsAccount(member); // 스레드 계정 정보 가져오기
            String accessToken = getAccessToken(member); // vault에서 가져온 액세스 토큰
            String mediaId = createMediaContainer(request.mediaType().getDescription(), request.media().getFirst(), accessToken, account.getUserId(), false, request.text());
            String publishedId = publishMediaContainer(mediaId, accessToken, account.getUserId());
            return savePosting(publishedId, projectId, request.mediaType().getDescription(), request.media().getFirst(), request.text());
        } catch (Exception e) {
            log.error("Error creating single media post on Threads: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }

    public String createUpdatedSingleMedia(Member member, PostingRequest request) {
        try {
            Account account = getThreadsAccount(member); // 스레드 계정 정보 가져오기
            String accessToken = getAccessToken(member); // vault에서 가져온 액세스 토큰
            String mediaId = createMediaContainer(request.mediaType().getDescription(), request.media().getFirst(), accessToken, account.getUserId(), false, request.text());
            return publishMediaContainer(mediaId, accessToken, account.getUserId());
        } catch (Exception e) {
            log.error("Error updating single media post on Threads: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }

    /**
     * 스레드 미디어 캐러셀 게시물 생성하기
     * @param member
     * @param request
     * @param projectId
     * @return ThreadsPostingResponse
     */
    public ThreadsPostingResponse createCarouselMedia(Member member, PostingRequest request, Long projectId) {
        try {
            Account account = getThreadsAccount(member);
            String accessToken = getAccessToken(member);
            List<String> mediaIds = new ArrayList<>();

            for (String mediaUrl : request.media()) {
                String mediaId = createMediaContainer(request.mediaType().getDescription(), mediaUrl, accessToken, account.getUserId(), true, null);
                mediaIds.add(mediaId);
            }

            URI containerUri = UriComponentsBuilder
                    .fromHttpUrl(THREADS_API_BASE + "/" + account.getUserId() + "/threads")
                    .queryParam("media_type", "CAROUSEL")
                    .queryParam("children", String.join(",", mediaIds))
                    .queryParam("access_token", accessToken)
                    .queryParam("text", request.text())
                    .build()
                    .encode()
                    .toUri();

            String containerId = String.valueOf(postToThreadsApi(containerUri).get("id"));
            String publishedId = publishMediaContainer(containerId, accessToken, account.getUserId());
            return savePosting(publishedId, projectId, request.mediaType().getDescription(), request.media().getFirst(), request.text());
        } catch (Exception e) {
            log.error("Error creating carousel media post on Threads: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }

    public String createUpdatedCarouselMedia(Member member, PostingRequest request) {
        try {
            Account account = getThreadsAccount(member);
            String accessToken = getAccessToken(member);
            List<String> mediaIds = new ArrayList<>();

            for (String mediaUrl : request.media()) {
                String mediaId = createMediaContainer(request.mediaType().getDescription(), mediaUrl, accessToken, account.getUserId(), true, null);
                mediaIds.add(mediaId);
            }

            URI containerUri = UriComponentsBuilder
                    .fromHttpUrl(THREADS_API_BASE + "/" + account.getUserId() + "/threads")
                    .queryParam("media_type", "CAROUSEL")
                    .queryParam("children", String.join(",", mediaIds))
                    .queryParam("access_token", accessToken)
                    .queryParam("text", request.text())
                    .build()
                    .encode()
                    .toUri();

            String containerId = String.valueOf(postToThreadsApi(containerUri).get("id"));
            return publishMediaContainer(containerId, accessToken, account.getUserId());
        } catch (Exception e) {
            log.error("Error updating carousel media post on Threads: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }
}