package org.zapply.product.domain.posting.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostingInfoResponse(
        @Schema(description = "게시물 ID", example = "1234567890")
        Long postingId,

        @Schema(description = "게시물 제목", example = "제목")
        String postingTitle,

        @Schema(description = "게시물 내용", example = "내용")
        String postingContent,

        @Schema(description = "예약 시간", example = "2023-10-01T12:00:00")
        LocalDateTime scheduledAt,

        @Schema(description = "플랫폼", example = "INSTAGRAM || FACEBOOK || TWITTER")
        SNSType postingType,

        @Schema(description = "게시글 상태", example = "DRAFT || PUBLISHED || SCHEDULED")
        PostingState postingState,

        @Schema(description = "게시글 링크(발행 시 생성)", example = "https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp")
        String postingLink,

        @Schema(description = "게시물 이미지 리스트", example = "[https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp, https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp]")
        List<String> postingImages
) {

    public static PostingInfoResponse of(Posting posting, List<String> images) {
        return PostingInfoResponse.builder()
                .postingId(posting.getPostingId())
                .postingTitle(posting.getTitle())
                .postingContent(posting.getPostingContent())
                .scheduledAt(posting.getScheduledAt())
                .postingType(posting.getPostingType())
                .postingState(posting.getPostingState())
                .postingLink(posting.getPostingLink())
                .postingImages(images)
                .build();
    }
}