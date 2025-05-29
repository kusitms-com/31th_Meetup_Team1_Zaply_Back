package org.zapply.product.global.snsClients.instagram;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstagramClient {
    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.registration.instagram.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.instagram.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.instagram.token-uri}")
    private String accessTokenUrl;

    @Value("${spring.security.oauth2.client.registration.instagram.authorization-grant-type}")
    private String grantType;

    @Value("${spring.security.oauth2.client.provider.instagram.user-info-uri}")
    private String userInfoUrl;

    @Value("${spring.security.oauth2.client.registration.instagram.redirect-uri}")
    private String redirectUri;


    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    /**
     * 인스타그램 로그인 URL 생성
     * @param memberId
     * @return 인스타그램 로그인 URL
     */
    public String buildAuthorizationUri(Long memberId) {
        String uri = redirectUri + "?state=" + memberId;
        return "https://www.facebook.com/dialog/oauth?client_id=" + clientId +
                "&redirect_uri=" + uri +
                "&display=page" +
                "&extras={\"setup\":{\"channel\":\"IG_API_ONBOARDING\"}}" +
                "&scope=instagram_basic,instagram_content_publish,instagram_manage_comments,instagram_manage_insights,pages_show_list,pages_read_engagement" +
                "&response_type=token";
    }

    /**
     * Instagram 비즈니스 계정 정보 조회
     * @param instagramBusinessAccountId Instagram 계정 ID (ex: 1784xxxxxxxx)
     * @param accessToken 액세스 토큰
     * @return InstagramProfile
     */
    public InstagramProfile getInstagramProfile(String instagramBusinessAccountId, String accessToken) {
        String url = String.format(
                "https://graph.facebook.com/v22.0/%s?fields=id,username,name,profile_picture_url&access_token=%s",
                instagramBusinessAccountId,
                accessToken
        );

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readValue(response, InstagramProfile.class);

        } catch (Exception e) {
            log.error("Error fetching Instagram business profile: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /**
     * 페이스북 페이지 목록과 Instagram 연결 정보 조회(인스타그램 비즈니스 계정 Id 조회용)
     * @param userAccessToken User Access Token
     * @return List of InstagramBusinessResponse.PageData
     */
    public List<InstagramBusinessResponse.PageData> getAccountId(String userAccessToken) {
        String url = String.format(
                "https://graph.facebook.com/v22.0/me/accounts" +
                        "?fields=id,name,access_token,instagram_business_account" +
                        "&access_token=%s", userAccessToken
        );

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            // JSON 응답 파싱
            InstagramBusinessResponse pagesResponse = objectMapper.readValue(response, InstagramBusinessResponse.class);

            // instagram_business_account가 있는 항목만 필터링
            List<InstagramBusinessResponse.PageData> filteredPages = pagesResponse.data().stream()
                    .filter(page -> page.instagramBusinessAccount() != null)
                    .collect(Collectors.toList());

            return filteredPages;

        } catch (Exception e) {
            log.error("Error getting Facebook pages: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /**
     * 장기 토큰 발행
     * @param shortLivedAccessToken
     * @return String
     */
    public String getLongLivedToken(String shortLivedAccessToken) {
        String url = String.format(
                "%s?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
                accessTokenUrl,
                clientId,
                clientSecret,
                shortLivedAccessToken
        );

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            InstagramToken tokenResponse = objectMapper.readValue(response, InstagramToken.class);
            return tokenResponse.accessToken();

        } catch (Exception e) {
            log.error("Error exchanging short-lived token for long-lived token: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }
}
