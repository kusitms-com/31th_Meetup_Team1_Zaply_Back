package org.zapply.product.domain.posting.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostingDetailResponse(
        @Schema(description = "게시물 ID", example = "1234567890")
        String postingId,

        @Schema(description = "게시물 제목", example = "제목")
        String postingTitle,

        @Schema(description = "게시물 내용", example = "내용")
        String postingContent,

        @Schema(description = "발행 시간", example = "2023-10-01T12:00:00")
        String publishedAt,

        @Schema(description = "이미지 URL", example = "[https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp, https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp]")
        List<String> postingImages
) {
    public static PostingDetailResponse of(
            String postingId,
            String postingTitle,
            String postingContent,
            String publishedAt,
            List<String> postingImages
    ) {
        return PostingDetailResponse.builder()
                .postingId(postingId)
                .postingTitle(postingTitle)
                .postingContent(postingContent)
                .publishedAt(publishedAt)
                .postingImages(postingImages)
                .build();
    }
}