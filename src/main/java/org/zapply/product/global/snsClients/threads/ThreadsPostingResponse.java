package org.zapply.product.global.snsClients.threads;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.zapply.product.domain.posting.entity.Posting;

@Builder
public record ThreadsPostingResponse(
        @Schema(description = "게시물 ID", example = "1234567890")
        String postingId,

        @Schema(description = "게시된 미디어 ID", example = "1234567890")
        String mediaId,

        @Schema(description = "미디어 타입", example = "IMAGE || VIDEO || TEXT")
        String mediaType,

        @Schema(description = "미디어 URL", example = "https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp")
        String media,

        @Schema(description = "미디어 텍스트", example = "미디어 텍스트")
        String text,

        @Schema(description = "게시물 생성 시간", example = "2023-10-01T12:00:00Z")
        String createdAt
) {
    public static ThreadsPostingResponse of(Posting posting, String mediaType, String media, String text) {
        return ThreadsPostingResponse.builder()
                .postingId(posting.getPostingId().toString())
                .mediaId(posting.getMediaId())
                .mediaType(mediaType)
                .media(media)
                .text(text)
                .createdAt(posting.getCreatedAt().toString())
                .build();
    }
}
