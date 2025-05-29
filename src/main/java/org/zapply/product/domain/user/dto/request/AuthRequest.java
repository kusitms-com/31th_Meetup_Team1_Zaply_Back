package org.zapply.product.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.zapply.product.domain.user.entity.Agreement;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.enumerate.LoginType;

@Builder
public record AuthRequest(

        @NotNull
        @Schema(description = "이름", example = "홍길동")
        String name,

        @NotNull
        @Schema(description = "이메일", example = "zaply123@gmail.com")
        String email,

        @NotNull
        @Schema(description = "휴대폰 번호", example = "010-1234-5678")
        String phoneNumber,

        @NotNull
        @Schema(description = "주민등록번호", example = "123456-1234567")
        String residentNumber,

        @NotNull
        @Schema(description = "비밀번호", example = "password123")
        String password,

        @NotNull
        @Schema(description = "이용약관 동의 여부", example = "true")
        Boolean termsOfServiceAgreed,

        @NotNull
        @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
        Boolean privacyPolicyAgreed,

        @NotNull
        @Schema(description = "마케팅 활용 수집 및 이용 동의", example = "false")
        Boolean marketingAgreed
) {

    public Agreement toAgreement() {
        return Agreement.builder()
                .termsOfServiceAgreed(termsOfServiceAgreed)
                .privacyPolicyAgreed(privacyPolicyAgreed)
                .marketingAgreed(marketingAgreed)
                .build();
    }

    public Member toMember(Credential credential) {
        return Member.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .residentNumber(residentNumber)
                .credential(credential)
                .agreement(toAgreement())
                .loginType(LoginType.DEFAULT)
                .build();
    }
}