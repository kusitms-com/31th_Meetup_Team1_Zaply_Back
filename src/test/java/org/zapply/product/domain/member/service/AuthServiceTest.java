package org.zapply.product.domain.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zapply.product.domain.member.util.AuthRequestUtilTest;
import org.zapply.product.domain.member.util.MemberUtilTest;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.dto.response.MemberResponse;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.service.AuthService;
import org.zapply.product.domain.user.service.CredentialService;
import org.zapply.product.domain.user.service.MemberService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
@DisplayName("🔑AuthService 테스트")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private MemberService memberService;

    @Nested
    @DisplayName("🔐회원가입 테스트")
    public class SignUp {
        @Test
        @DisplayName("✅회원가입 성공")
        void signUp_success() {
            // given
            AuthRequest authRequest = AuthRequestUtilTest.createAuthRequest();

            Member member = MemberUtilTest.createMember();
            Credential credential = member.getCredential();

            Mockito.when(credentialService.createCredential(authRequest)).thenReturn(credential);
            Mockito.when(memberService.createMember(credential, authRequest)).thenReturn(member);

            // when
            MemberResponse result = authService.signUp(authRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(member.getId());
            assertThat(result.name()).isEqualTo(member.getName());
            assertThat(result.email()).isEqualTo(member.getEmail());
        }

        @Test
        @DisplayName("❌회원가입 실패 - MemberAlreadyExists")
        void signUp_fail() {
            // given
            AuthRequest authRequest = AuthRequestUtilTest.createAuthRequest();
            Credential credential = Credential.builder()
                    .hashedPassword("hashedPassword")
                    .build();

            Mockito.when(credentialService.createCredential(authRequest)).thenReturn(credential);
            Mockito.when(memberService.createMember(credential, authRequest))
                    .thenThrow(new CoreException(GlobalErrorType.MEMBER_ALREADY_EXISTS));

            // when & then
            try {
                authService.signUp(authRequest);
            } catch (CoreException e) {
                assertThat(e.getErrorType()).isEqualTo(GlobalErrorType.MEMBER_ALREADY_EXISTS);
            }
        }
    }
}