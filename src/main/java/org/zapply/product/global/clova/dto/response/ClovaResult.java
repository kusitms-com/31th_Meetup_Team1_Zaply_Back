package org.zapply.product.global.clova.dto.response;

import org.zapply.product.global.clova.dto.ClovaMessage;

public record ClovaResult(
        ClovaMessage message,
        String stopReason,
        int inputLength,
        int outputLength
) {
}
