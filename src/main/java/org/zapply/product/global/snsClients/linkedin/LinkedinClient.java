package org.zapply.product.global.snsClients.linkedin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedinClient {
    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.provider.linkedin.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.registration.linkedin.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.linkedin.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.linkedin.token-uri}")
    private String accessTokenUrl;

    @Value("${spring.security.oauth2.client.registration.linkedin.authorization-grant-type}")
    private String grantType;

    @Value("${spring.security.oauth2.client.provider.linkedin.user-info-uri}")
    private String userInfoUrl;

    @Value("${spring.security.oauth2.client.registration.linkedin.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    /**
     * 링크드인 로그인 URL 생성
     * @param memberId
     * @return 스레드 로그인 URL
     */
    public String buildAuthorizationUri(Long memberId) {
        return UriComponentsBuilder
                .fromHttpUrl(authorizationUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid,profile,w_member_social,email")
                .queryParam("state", memberId)
                .toUriString();
    }

    /**
     * LinkedIn으로부터 받은 인가코드를 통해 액세스 토큰 요청하기
     * @param code
     * @return LinkedinToken
     */
    public LinkedinToken getLinkedinAccessToken(String code) {
        String cleanCode = URLDecoder.decode(code, StandardCharsets.UTF_8).split("#")[0];
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", grantType);
        form.add("code", cleanCode);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        String resp = restClient.post()
                .uri(accessTokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(resp, LinkedinToken.class);
        } catch (Exception e) {
            log.error("LinkedIn 토큰 파싱 실패", e);
            throw new CoreException(GlobalErrorType.LINKEDIN_API_ERROR);
        }
    }

    public LinkedinUserInfo getLinkedinProfile(String accessToken) {
        try {
            String resp = restClient.get()
                    .uri(userInfoUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            log.info("LinkedIn 프로필 조회 응답: {}", resp);
            return objectMapper.readValue(resp, LinkedinUserInfo.class);
        } catch (Exception e) {
            log.error("LinkedIn 프로필 조회 실패", e);
            throw new CoreException(GlobalErrorType.LINKEDIN_API_ERROR);
        }
    }
}
