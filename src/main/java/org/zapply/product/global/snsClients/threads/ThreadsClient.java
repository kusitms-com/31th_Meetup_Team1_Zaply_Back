package org.zapply.product.global.snsClients.threads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadsClient {
    private final MemberRepository memberRepository;
    @Value("${spring.security.oauth2.client.registration.threads.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.threads.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.threads.token-uri}")
    private String accessTokenUrl;

    @Value("${spring.security.oauth2.client.registration.threads.authorization-grant-type}")
    private String grantType;

    @Value("${spring.security.oauth2.client.provider.threads.user-info-uri}")
    private String userInfoUrl;

    @Value("${spring.security.oauth2.client.registration.threads.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    /**
     * 스레드 로그인 URL 생성
     * @param memberId
     * @return 스레드 로그인 URL
     */
    public String buildAuthorizationUri(Long memberId) {
        String uri = redirectUri + "?state=" + memberId;
        return UriComponentsBuilder.fromHttpUrl("https://threads.net/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "threads_basic,threads_content_publish,threads_manage_replies,threads_manage_insights,threads_read_replies,threads_manage_mentions,threads_keyword_search,threads_delete")
                .queryParam("response_type", "code")
                .toUriString();
    }


    /**
     * 스레드로부터 받은 인가코드를 통해 액세스 토큰 요청하기
     * @param code
     * @param redirectUri
     * @return ThreadsToken
     */
    public ThreadsToken getThreadsAccessToken(String code, String redirectUri) {

        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        String cleanCode = decodedCode.split("#")[0];  // code 이후의 #_ 제거

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", grantType);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", cleanCode);

        String response = restClient.post()
                .uri(accessTokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(response, ThreadsToken.class);
        } catch (Exception e) {
            log.info("스레드 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }

    /**
     * 스레드 프로필 정보 요청하기
     * @param accessToken
     * @return ThreadsProfile
     */
    public ThreadsProfile getThreadsProfile(String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(userInfoUrl)
                .queryParam("access_token", accessToken)
                .toUriString();

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readValue(response, ThreadsProfile.class);
        } catch (Exception e) {
            log.info("스레드 프로필 정보 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }

    /**
     * 스레드에서 발급한 단기 액세스 토큰을 통해 장기 액세스 토큰 요청하기
     * @param shortLivedAccessToken
     * @return ThreadsToken
     */
    public ThreadsToken getLongLivedToken(String shortLivedAccessToken) {
        String url = UriComponentsBuilder.fromHttpUrl("https://graph.threads.net/access_token")
                .queryParams(new LinkedMultiValueMap<>(Map.of(
                        "grant_type", List.of("th_exchange_token"),
                        "client_secret", List.of(clientSecret),
                        "access_token", List.of(shortLivedAccessToken)
                )))
                .toUriString();
        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readValue(response, ThreadsToken.class);
        } catch (JsonProcessingException e) {
            log.info("스레드 장기 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.JSON_PROCESSING_ERROR);
        } catch (Exception e) {
            log.info("스레드 장기 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_API_ERROR);
        }
    }
}
