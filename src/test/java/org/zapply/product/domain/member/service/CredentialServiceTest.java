package org.zapply.product.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zapply.product.domain.member.util.AuthRequestUtilTest;
import org.zapply.product.domain.member.util.MemberUtilTest;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.CredentialRepository;
import org.zapply.product.domain.user.service.CredentialService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Nested
@DisplayName("🔑CredentialService 테스트")
@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @InjectMocks
    private CredentialService credentialService;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = AuthRequestUtilTest.createAuthRequest();
    }

    @Nested
    @DisplayName("🔐createCredential() 테스트")
    class CreateCredentialTest {

        @Test
        @DisplayName("✅비밀번호 해싱 후 Credential 저장 성공")
        void createCredential_success() {
            // given
            String hashedPassword = "hashedPassword123!";
            when(passwordEncoder.encode(authRequest.password()))
                    .thenReturn(hashedPassword);

            Credential savedCredential = Credential.builder()
                    .hashedPassword(hashedPassword)
                    .build();

            when(credentialRepository.save(any(Credential.class)))
                    .thenReturn(savedCredential);

            // when
            Credential result = credentialService.createCredential(authRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPassword()).isEqualTo(hashedPassword);
            verify(credentialRepository).save(any(Credential.class));
        }
    }

    @Nested
    @DisplayName("🔐checkPassword() 테스트")
    class CheckPasswordTest {

        private Member member;
        private Credential credential;

        @BeforeEach
        void setUp() {
            credential = Credential.builder()
                    .hashedPassword("hashedPassword123!")
                    .build();

            member = MemberUtilTest.createMemberWithCredential(credential);
        }

        @Test
        @DisplayName("✅비밀번호 일치")
        void checkPassword_success() {
            // given
            when(passwordEncoder.matches("비밀번호123!", credential.getPassword()))
                    .thenReturn(true);

            // when
            boolean result = credentialService.checkPassword(member, "비밀번호123!");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("❌ 비밀번호 불일치 -> CoreException")
        void checkPassword_fail() {
            // given
            when(passwordEncoder.matches("틀린비밀번호!", credential.getPassword()))
                    .thenReturn(false);

            // when & then
            assertThatThrownBy(() -> credentialService.checkPassword(member, "틀린비밀번호!"))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining(GlobalErrorType.PASSWORD_MISMATCH.getMessage());
        }
    }
}