package org.zapply.product.global.snsClients.facebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.snsClients.facebook.FacebookProfile;
import org.zapply.product.global.snsClients.facebook.FacebookToken;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FacebookClient {

    private static final Logger log = LoggerFactory.getLogger(FacebookClient.class);
    private final MemberRepository memberRepository;
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.facebook.token-uri}")
    private String accessTokenUrl;

    @Value("${spring.security.oauth2.client.provider.facebook.user-info-uri}")
    private String userInfoUrl;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String redirectUri;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();
    private static final String FB_GRAPH_BASE = "https://graph.facebook.com/v22.0";

    /**
     * 페이스북 로그인 URL 생성
     * @return String
     */
    public String buildAuthorizationUri(Member member) {
        String uri = redirectUri + "?state=" + member.getId();

        return "https://www.facebook.com/v22.0/dialog/oauth?client_id=" + clientId +
                "&redirect_uri=" + uri +
                "&scope=email";
    }

    /**
     * 페이스북으로부터 받은 인가코드를 통해 액세스 토큰 요청하기
     * @param code
     * @param redirectUri
     * @return FacebookToken
     */
    public FacebookToken getFacebookAccessToken(String code, String redirectUri) {
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        String cleanCode = decodedCode.split("#")[0];  // code 이후의 #_=_ 부분을 분리

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("graph.facebook.com")
                        .path("/oauth/access_token")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", cleanCode)
                        .build())
                .retrieve()
                .body(String.class);


        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return new FacebookToken(jsonNode.get("access_token").asText());
        } catch (IOException e) {
            log.info("페이스북 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    /**
     * 페이스북에 있는 사용자 정보 반환
     * @param token
     * @return FacebookProfile
     */
    public FacebookProfile getMemberInfo(String token) {
        String response = restClient.get()
                .uri(userInfoUrl + token)
                .retrieve()
                .body(String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            FacebookProfile.Picture picture = null;

            if (jsonNode.has("picture")) {
                JsonNode pictureNode = jsonNode.get("picture");
                if (pictureNode.has("data") && pictureNode.get("data").has("url")) {
                    String url = pictureNode.get("data").get("url").asText();

                    picture = new FacebookProfile.Picture(
                            new FacebookProfile.Picture.Data(url)
                    );
                }
            }

            return new FacebookProfile(
                    jsonNode.get("id").asText(),
                    jsonNode.get("name").asText(),
                    jsonNode.has("email") ? jsonNode.get("email").asText() : null,
                    picture
            );
        } catch (IOException e) {
            log.info("페이스북 사용자 정보 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    /**
     * 페이스북에서 받은 단기 액세스 토큰을 통해 장기 액세스 토큰 요청하기
     * @param shortLivedToken
     * @return FacebookToken
     */
    public FacebookToken getLongLivedToken(String shortLivedToken) {
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("graph.facebook.com")
                        .path("/oauth/access_token")
                        .queryParam("grant_type", "fb_exchange_token")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("fb_exchange_token", shortLivedToken)
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return new FacebookToken(jsonNode.get("access_token").asText());
        } catch (IOException e) {
            log.info("페이스북 장기 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    public FacebookToken getPageAccessToken(String userId, String userAccessToken) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FB_GRAPH_BASE + "/" + userId + "/accounts").build().encode().toUri();
        try {
            String response = restClient.get().uri(uri)
                    .header("Authorization", "Bearer " + userAccessToken)
                    .retrieve().body(String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            return new FacebookToken(jsonNode.at("/data/0/access_token").asText(null));
        } catch (IOException e) {
            log.info("페이스북 페이지 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }


    public FacebookToken getLongLivedPageAccessToken(String userId, String userAccessToken) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FB_GRAPH_BASE + "/" + userId + "/accounts").build().encode().toUri();
        try {
            String response = restClient.get().uri(uri)
                    .header("Authorization", "Bearer " + userAccessToken)
                    .retrieve().body(String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            return new FacebookToken(jsonNode.at("/data/0/access_token").asText(null));
        } catch (IOException e) {
            log.info("페이스북 페이지 액세스 토큰 요청 실패: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }


}