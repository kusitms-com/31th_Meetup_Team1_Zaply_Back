package org.zapply.product.domain.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zapply.product.domain.member.util.AuthRequestUtilTest;
import org.zapply.product.domain.member.util.MemberUtilTest;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.domain.user.service.MemberService;
import org.zapply.product.global.apiPayload.exception.CoreException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Nested
@DisplayName("🙆MemberService 테스트")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    private AuthRequest authRequest;
    private Credential credential;

    @BeforeEach
    void setUp() {
        authRequest = AuthRequestUtilTest.createAuthRequest();
        credential = MemberUtilTest.createCredential();
    }

    @Nested
    @DisplayName("🙆‍createMember() 테스트")
    public class CreateMember {
        @Test
        @DisplayName("✅createMember() 성공")
        void createMember_success () {
            // given
            when(memberRepository.findByEmailAndDeletedAtIsNull(authRequest.email()))
                    .thenReturn(Optional.empty());

            Member dummyMember = Member.builder()
                            .name(authRequest.name())
                            .email(authRequest.email())
                            .phoneNumber(authRequest.phoneNumber())
                            .credential(credential)
                            .build();

            when(memberRepository.save(any(Member.class)))
                    .thenReturn(dummyMember);

            // when
            Member result = memberService.createMember(credential, authRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(authRequest.name());
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("❌createMember() 실패 - MemberAlreadyExists")
        void createMember_fail_memberAlreadyExists () {
            // given
            when(memberRepository.findByEmailAndDeletedAtIsNull(authRequest.email()))
                    .thenReturn(Optional.of(MemberUtilTest.createMember()));

            // when & then
            assertThatThrownBy(() -> memberService.createMember(credential, authRequest))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("이미 존재하는 멤버입니다");

            verify(memberRepository, never()).save(any());
        }
    }
}
