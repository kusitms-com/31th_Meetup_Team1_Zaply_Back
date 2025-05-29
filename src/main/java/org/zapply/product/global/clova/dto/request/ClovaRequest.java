package org.zapply.product.global.clova.dto.request;

import org.zapply.product.global.clova.dto.ClovaMessage;

import java.util.List;

public record ClovaRequest(
        List<ClovaMessage> messages,
        int maxTokens
) {
    public static ClovaRequest from(List<ClovaMessage> messages) {
        return new ClovaRequest(messages, 1024);
    }
}

