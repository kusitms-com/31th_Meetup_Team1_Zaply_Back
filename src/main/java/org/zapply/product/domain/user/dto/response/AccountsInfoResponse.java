package org.zapply.product.domain.user.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AccountsInfoResponse(
        int totalCount,
        List<AccountInfo> accounts
) {
    public static AccountsInfoResponse of(List<AccountInfo> accountInfos) {
        return AccountsInfoResponse.builder()
                .totalCount(accountInfos.size())
                .accounts(accountInfos)
                .build();
    }
}