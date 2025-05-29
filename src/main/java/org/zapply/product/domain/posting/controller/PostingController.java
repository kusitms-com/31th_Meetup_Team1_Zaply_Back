package org.zapply.product.domain.posting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zapply.product.domain.posting.dto.request.PostingRequest;
import org.zapply.product.domain.posting.dto.response.CursorSlice;
import org.zapply.product.domain.posting.dto.response.PostingDetailResponse;
import org.zapply.product.domain.posting.dto.response.PostingInfoResponse;
import org.zapply.product.domain.posting.service.FacebookPostingService;
import org.zapply.product.domain.posting.service.InstagramPostingService;
import org.zapply.product.domain.posting.service.PostingQueryService;
import org.zapply.product.domain.posting.service.PublishPostingService;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.global.security.AuthDetails;
import org.zapply.product.global.snsClients.threads.ThreadsInsightClient;
import org.zapply.product.global.snsClients.threads.ThreadsInsightResponse;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/posting")
@RequiredArgsConstructor
@Tag(name = "Posting", description = "게시글 발행 API")
public class PostingController {

    private final PublishPostingService publishPostingService;
    private final PostingQueryService postingQueryService;
    private final ThreadsInsightClient threadsInsightClient;
    private final InstagramPostingService instagramPostingService;
    private final FacebookPostingService facebookPostingService;

    // 사용자의 프로젝트에 존재하는 포스팅 조회를 위한 API
    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트(컨텐츠)별 포스팅 조회", description = "프로젝트(컨텐츠)별 발행된 포스팅 내용 및 예약시간 조회")
    public ApiResponse<List<PostingInfoResponse>> getProjectList(@AuthenticationPrincipal AuthDetails authDetails,
                                                                 @PathVariable("projectId") Long projectId){
        return ApiResponse.success(postingQueryService.getPostings(authDetails.getMember(), projectId));
    }
    @PostMapping("/threads/{projectId}/single")
    @Operation(summary = "threads single Media 즉시 발행하기", description = "단일 미디어를 업로드하는 메소드. (media 하나만 업로드)")
    public ApiResponse<?> createSingleMedia(@AuthenticationPrincipal AuthDetails authDetails,
                                            @Valid @RequestBody PostingRequest request,
                                            @PathVariable("projectId") Long projectId) {
        if (request.scheduledAt() != null) {
            publishPostingService.scheduleSingleMediaPublish(request, projectId, SNSType.THREADS);
        }
        else{
            publishPostingService.publishSingleMediaNow(authDetails.getMember(), request, projectId);
        }
        return ApiResponse.success();
    }

    @PutMapping("{postingId}/single/reschedule")
    @Operation(summary = "single Media 발행 시간 및 발행 글 수정하기", description = "SNS타입을 인자로 받아서 발행시점 및 발행글을 수정함")
    public ApiResponse<?> updateSingleMediaSchedule(@Valid @RequestBody PostingRequest request,
                                                    @PathVariable("postingId") Long postingId,
                                                    @RequestParam("snsType") SNSType snsType) {
        publishPostingService.rescheduleSingleMedia(postingId, request.scheduledAt(), snsType, request.text());
        return ApiResponse.success();
    }

    @PostMapping("/threads/{projectId}/carousel")
    @Operation(summary = "threads carousel 즉시 발행하기", description = "캐러셀 미디어를 업로드하는 메소드. (media 여러개 업로드)")
    public ApiResponse<?> createCarouselMedia(@AuthenticationPrincipal AuthDetails authDetails,
                                              @Valid @RequestBody PostingRequest request,
                                              @PathVariable("projectId") Long projectId) {
        if (request.scheduledAt() != null) {
            publishPostingService.scheduleCarouselMediaPublish(request, projectId, SNSType.THREADS);
        }
        else{
            publishPostingService.publishCarouselMediaNow(authDetails.getMember(), request, projectId);
        }
        return ApiResponse.success();
    }

    @PutMapping("{postingId}/carousel/reschedule")
    @Operation(summary = "carousel Media 발행 시간 및 발행 글 수정하기", description = "SNS타입을 인자로 받아서 발행시점 및 발행 글을 수정합니다")
    public ApiResponse<?> updateCarouselSchedule(@Valid @RequestBody PostingRequest request,
                                                 @PathVariable("postingId") Long postingId,
                                                 @RequestParam("snsType") SNSType snsType) {
        publishPostingService.rescheduleCarouselMedia(postingId, request.scheduledAt(), snsType, request.text() );
        return ApiResponse.success();
    }

    @GetMapping("/threads/{postingId}/insight")
    @Operation(summary = "스레드 게시물 인사이트 조회하기", description = "스레드 게시물의 인사이트를 조회하는 메소드.")
    public ApiResponse<ThreadsInsightResponse> getThreadsInsight(@AuthenticationPrincipal AuthDetails authDetails,
                                                                 @PathVariable("postingId") Long postingId) {
        return ApiResponse.success(
                threadsInsightClient.getThreadsInsight(authDetails.getMember(), postingId));
    }

    @GetMapping("/my-media")
    @Operation(summary = "SNS 게시물 리스트 조회하기", description = "SNS 게시물 리스트를 조회하는 메소드.")
    public ApiResponse<CursorSlice<PostingDetailResponse>> getMedia(@RequestParam(name = "cursor", required = false) String cursor,
                                                                           @RequestParam(name = "size", defaultValue = "9") int size,
                                                                           @RequestParam("snsType") SNSType snsType,
                                                                           @AuthenticationPrincipal AuthDetails authDetails) {
        switch (snsType) {
            case THREADS:
                return ApiResponse.success(postingQueryService.getAllThreadsMedia(cursor, size, authDetails.getMember()));
            case INSTAGRAM:
                return ApiResponse.success(postingQueryService.getAllInstagramMedia(authDetails.getMember(), cursor, size));
            case FACEBOOK:
                return ApiResponse.success(postingQueryService.getAllFacebookMedia(authDetails.getMember(), cursor, size));
            default:
                return ApiResponse.success(null);
        }
    }

    @GetMapping("/media")
    @Operation(summary = "SNS 단일 게시물 조회하기", description = "SNS 단일 게시물을 조회하는 메소드.")
    public ApiResponse<PostingDetailResponse> getSingleMedia(@RequestParam("snsType") SNSType snsType,
                                                                    @RequestParam("mediaId") String mediaId,
                                                                    @AuthenticationPrincipal AuthDetails authDetails) {
        switch (snsType) {
            case THREADS:
                return ApiResponse.success(postingQueryService.getSingleThreadsMedia(authDetails.getMember(), mediaId));
            case INSTAGRAM:
                return ApiResponse.success(postingQueryService.getSingleInstagramMedia(authDetails.getMember(), mediaId));
            case FACEBOOK:
                return ApiResponse.success(postingQueryService.getSingleFacebookMedia(authDetails.getMember(), mediaId));
            default:
                return ApiResponse.success(null);
        }
    }

    @PostMapping("/instagram/{projectId}/carousel")
    @Operation(summary = "인스타그램 캐러셀 미디어 발행하기", description = "인스타그램 캐러셀 미디어 발행하기 메소드.")
    public ApiResponse<?> createInstagramCarouselMedia(@AuthenticationPrincipal AuthDetails authDetails,
                                                       @Valid @RequestBody PostingRequest request,
                                                       @PathVariable("projectId") Long projectId) {
        if (request.scheduledAt() != null) {
            publishPostingService.scheduleCarouselMediaPublish(request, projectId, SNSType.INSTAGRAM);
        }
        else{
            instagramPostingService.publishInstagramCarousel(
                    authDetails.getMember(), request, projectId);
        }
        return ApiResponse.success();
    }

    @PostMapping("/facebook/{projectId}/carousel")
    @Operation(summary = "페이스북 캐러셀 미디어 발행하기", description = "페이스북 캐러셀 미디어 발행하기 메소드.")
    public ApiResponse<?> createFacebookCarouselMedia(@AuthenticationPrincipal AuthDetails authDetails,
                                                       @Valid @RequestBody PostingRequest request,
                                                       @PathVariable("projectId") Long projectId) {
        if (request.scheduledAt() != null) {
            publishPostingService.scheduleCarouselMediaPublish(request, projectId, SNSType.FACEBOOK);
        }
        else{
            facebookPostingService.publishPostWithMultiPhotoAndText(authDetails.getMember(), request.text(), request.media());
        }
        return ApiResponse.success();
    }

    @PostMapping("/facebook/{projectId}/single")
    @Operation(summary = "페이스북 싱글 미디어 발행하기", description = "페이스북 싱글 미디어 발행하기 메소드.")
    public ApiResponse<?> createFacebookSingleMedia(@AuthenticationPrincipal AuthDetails authDetails,
                                                      @Valid @RequestBody PostingRequest request,
                                                      @PathVariable("projectId") Long projectId) {
        if (request.scheduledAt() != null) {
            publishPostingService.scheduleSingleMediaPublish(request, projectId, SNSType.FACEBOOK);
        }
        else{
            facebookPostingService.publishPostWithSinglePhotoAndText(authDetails.getMember(), request.text(), request.media().getFirst());
        }
        return ApiResponse.success();
    }
}