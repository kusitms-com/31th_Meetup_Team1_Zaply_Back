package org.zapply.product.domain.posting.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.util.List;

public record ToneTransferRequest(
        @Schema(description = "SNS 타입", example = "[\"INSTAGRAM\", \"FACEBOOK\"]")
        List<@NotNull SNSType> snsTypes,

        @NotNull
        @Schema(description = "사용자 입력 콘텐츠", example = "오늘 점심 뭐 먹을지 고민이야.")
        String userPrompt
) {
}
