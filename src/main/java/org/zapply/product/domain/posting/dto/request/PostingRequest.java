package org.zapply.product.domain.posting.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.enumerate.MediaType;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostingRequest(
        @NotNull
        @Schema(description = "미디어 타입", example = "IMAGE || VIDEO || TEXT")
        MediaType mediaType,

        @Schema(description = "미디어 제목", example = "미디어 제목")
        String title,

        @NotNull
        @Schema(description = "미디어 URL들",
                example = "[\"https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp\"," +
                        " \"https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp\"]")
        List<String> media,

        @NotNull
        @Schema(description = "미디어 텍스트", example = "미디어 텍스트")
        @Max(value = 500, message = "미디어 텍스트는 500자 이내로 입력해주세요")
        String text,

        @NotNull
        @Schema(description = "예약 실행 시각 (분 단위, ISO-8601)", example = "2025-05-15T18:30")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        LocalDateTime scheduledAt
) {
        public static PostingRequest of(MediaType mediaType, List<String> imageUrls, Posting posting){
                return PostingRequest.builder()
                        .mediaType(mediaType)
                        .media(imageUrls)
                        .title(posting.getTitle())
                        .text(posting.getPostingContent())
                        .scheduledAt(posting.getScheduledAt())
                        .build();
        }
}
