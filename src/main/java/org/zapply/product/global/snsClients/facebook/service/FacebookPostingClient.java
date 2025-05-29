package org.zapply.product.global.snsClients.facebook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.user.entity.Account;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.AccountRepository;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookPostingClient {

    private static final String FB_GRAPH_BASE = "https://graph.facebook.com/v22.0";

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    private Account getFacebookAccount(Member member) {
        return accountRepository.findByAccountTypeAndMember(SNSType.FACEBOOK, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_NOT_FOUND));
    }

    public String createSinglePost(String accessToken, String pageId, String message) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FB_GRAPH_BASE + "/" + pageId + "/feed")
                .queryParam("message", message).build().encode().toUri();
        try {
            String response = restClient.post()
                    .uri(uri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("id").asText();
        } catch (Exception e) {
            log.error("Error creating Facebook page post", e);
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    public String publishSinglePhotoAndPost(String accessToken, String pageId, String message, String photoUrl) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(FB_GRAPH_BASE + "/" + pageId + "/photos")
                .queryParam("url", photoUrl)
                .queryParam("message", message)
                .queryParam("published", true)
                .build().encode().toUri();

        try{
            @SuppressWarnings("unchecked")
            String response = restClient.post().uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve().body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.asText();
        } catch (Exception e) {
            log.error("Error creating Facebook page post", e);
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    public String createPagePost(String accessToken, String pageId, String message, List<String> photoUrls) {
        try {
            URI feedUri = UriComponentsBuilder
                    .fromHttpUrl(FB_GRAPH_BASE + "/" + pageId + "/feed")
                    .build().encode()
                    .toUri();

            // --- 사진이 있으면 unpublished 업로드 후 attached_media 배열 생성 ---
            Map<String,Object> payload = new HashMap<>();
            if (photoUrls != null && !photoUrls.isEmpty()) {
                List<Map<String,String>> attached = photoUrls.stream()
                        .map(url -> Map.of("media_fbid", uploadUnpublishedPhoto(pageId, accessToken, url)))
                        .collect(Collectors.toList());
                payload.put("attached_media", attached);
            }

            // --- 메시지가 있으면 payload 에 삽입 ---
            if (message != null && !message.isBlank()) {
                payload.put("message", message);
            }

            // --- case: 사진도 없고 메시지도 없으면 예외 처리 ---
            if (payload.isEmpty()) {
                throw new IllegalArgumentException("message와 photoUrls 중 최소 하나는 제공되어야 합니다.");
            }

            String response = restClient.post()
                    .uri(feedUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.get("id").asText();

        } catch (Exception ex) {
            log.error("Error creating Facebook page post", ex);
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    /**
     * helper: 사진 URL을 unpublished 상태로 업로드하고 media_fbid만 반환
     */
    private String uploadUnpublishedPhoto(String pageId, String token, String photoUrl) {
        URI photoUri = UriComponentsBuilder
                .fromHttpUrl(FB_GRAPH_BASE + "/" + pageId + "/photos")
                .queryParam("url", photoUrl)
                .queryParam("published", false)
                .build().encode()
                .toUri();
        // 1) raw JSON string 으로 받기
        String raw = restClient.post()
                .uri(photoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                // Accept JSON 로 명시해도 좋습니다
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .body(String.class);

        try {
            // 2) ObjectMapper로 Map으로 변환
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> photoResp = mapper.readValue(
                    raw,
                    new TypeReference<Map<String,Object>>() {}
            );
            return (String) photoResp.get("id");
        } catch (Exception e) {
            log.error("Error parsing Facebook photo upload response", e);
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

}
