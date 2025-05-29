package org.zapply.product.domain.member.util;

import org.zapply.product.domain.user.dto.request.AuthRequest;

public class AuthRequestUtilTest {
    public static AuthRequest createAuthRequest() {
        return AuthRequest.builder()
                .email("zaply123@gmail.com")
                .password("비밀번호123!")
                .name("zaply")
                .phoneNumber("010-1234-5678")
                .residentNumber("123456-1234567")
                .termsOfServiceAgreed(true)
                .privacyPolicyAgreed(true)
                .marketingAgreed(false)
                .build();
    }
}
