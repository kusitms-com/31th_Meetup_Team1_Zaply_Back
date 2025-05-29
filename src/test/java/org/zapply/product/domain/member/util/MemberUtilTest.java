package org.zapply.product.domain.member.util;

import org.zapply.product.domain.user.entity.Agreement;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.enumerate.LoginType;

public class MemberUtilTest {

    public static Member createMember() {
        return Member.builder()
                .email("zaply123@gmail.com")
                .name("zaply")
                .phoneNumber("010-1234-5678")
                .residentNumber("123456-1234567")
                .agreement(createAgreement())
                .credential(createCredential())
                .loginType(LoginType.DEFAULT)
                .build();
    }

    public static Member createMemberWithCredential(Credential credential) {
        return Member.builder()
                .email("zaply123@gmail.com")
                .name("zaply")
                .phoneNumber("010-1234-5678")
                .residentNumber("123456-1234567")
                .agreement(createAgreement())
                .credential(credential)
                .loginType(LoginType.DEFAULT)
                .build();
    }

    public static Agreement createAgreement() {
        return Agreement.builder()
                .marketingAgreed(true)
                .privacyPolicyAgreed(true)
                .termsOfServiceAgreed(true)
                .build();
    }

    public static Credential createCredential(){
        return Credential.builder()
                .hashedPassword("hashedPassword")
                .build();
    }
}
