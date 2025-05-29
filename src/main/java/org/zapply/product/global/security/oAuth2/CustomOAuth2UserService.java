package org.zapply.product.global.security.oAuth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.user.entity.Agreement;
import org.zapply.product.domain.user.entity.Credential;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.enumerate.LoginType;
import org.zapply.product.domain.user.repository.MemberRepository;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    /**
     * OAuth2UserService를 통해 OAuth2User 정보를 가져온 후,
     * DB에 회원이 없으면 신규 생성하고, 있으면 기존 회원 정보를 반환
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 OAuth2 서비스 구분 (google, facebook 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // OAuth2 로그인 시 key로 사용되는 필드 (PK)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 사용자 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Google 사용자 정보 추출
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // agreement 정보는 기본값으로 설정
        Agreement agreement = Agreement.builder()
                .privacyPolicyAgreed(false)
                .termsOfServiceAgreed(false)
                .marketingAgreed(false)
                .build();

        // 회원 조회 또는 생성
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseGet(() -> {
                    log.info("google 로그인 신규 회원 생성: {}", email);
                    return memberRepository.save(
                            Member.builder()
                                    .name(name)
                                    .email(email)
                                    .agreement(agreement)
                                    .loginType(LoginType.GOOGLE)
                                    .build()
                    );
                });

        return new CustomOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                userNameAttributeName,
                email,
                member.getId()
        );
    }
}