package org.zapply.product.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LoginResponse(
        @Schema(description = "토큰 정보", example = "액세스토큰, 리프레쉬 토큰")
        TokenResponse tokenResponse,

        @Schema(description = "사용자 정보", example = "회원 ID, 이름, 이메일, 휴대폰 번호")
        MemberResponse memberResponse,

        @Schema(description = "연동된 계정 정보", example = "연동계정 갯수, 연동계정 플랫폼, 연동계정 이름")
        AccountsInfoResponse accountsInfoResponse
) {
    public static LoginResponse of(TokenResponse tokenResponse, MemberResponse memberResponse, AccountsInfoResponse accountsInfoResponse) {
        return LoginResponse.builder()
                .tokenResponse(tokenResponse)
                .memberResponse(memberResponse)
                .accountsInfoResponse(accountsInfoResponse)
                .build();
    }
}
