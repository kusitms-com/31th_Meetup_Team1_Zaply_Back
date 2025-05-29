package org.zapply.product.domain.posting.dto.response;

import lombok.Builder;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.util.List;

@Builder
public record ToneTransferResponse(
        List<ToneTransferResponseItem> toneTransferResponseItems
) {
    @Builder
    public record ToneTransferResponseItem(
            SNSType snsType,
            String content
    ) {
    }
}
