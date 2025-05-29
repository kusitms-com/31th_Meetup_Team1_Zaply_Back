package org.zapply.product.global.snsClients.instagram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class InstagramMediaClient {

    private static final Logger log = LoggerFactory.getLogger(InstagramMediaClient.class);
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public Account getInstagramAccount(Member member) {
        return accountRepository.findByAccountTypeAndMember(SNSType.INSTAGRAM, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_NOT_FOUND));
    }

    private String getAccessToken(Member member) {
        return accountService.getAccessToken(member, SNSType.INSTAGRAM);
    }

    public List<InstagramMediaResponse.Data> getAllMedia(Member member) {
        Account account = getInstagramAccount(member);
        String userId = account.getUserId();
        String accessToken = getAccessToken(member);

        List<InstagramMediaResponse.Data> allMedia = new ArrayList<>();
        String url = String.format("https://graph.facebook.com/v22.0/%s/media?fields=id,caption,media_type,media_url,permalink,timestamp&access_token=%s", userId, accessToken);

        while (url != null) {
            try {
                String rawResponse = restClient.get()
                        .uri(URI.create(url))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(rawResponse);
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray()) {
                    List<InstagramMediaResponse.Data> mediaList = StreamSupport.stream(dataArray.spliterator(), false)
                            .map(node -> buildMediaData(node, accessToken))
                            .toList();
                    allMedia.addAll(mediaList);
                }

                JsonNode paging = root.get("paging");
                url = (paging != null && paging.has("next")) ? paging.get("next").asText() : null;

            } catch (Exception e) {
                log.error("Instagram API Error: {}", e.getMessage());
                throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
            }
        }

        return allMedia;
    }

    private Set<String> getCarouselChildren(String carouselId, String accessToken) {
        String url = String.format("https://graph.facebook.com/v22.0/%s/children?fields=media_url&access_token=%s", carouselId, accessToken);

        Set<String> mediaUrls = new LinkedHashSet<>();

        try {
            String rawResponse = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode dataArray = root.get("data");

            if (dataArray != null && dataArray.isArray()) {
                mediaUrls.addAll(StreamSupport.stream(dataArray.spliterator(), false)
                        .map(node -> node.get("media_url").asText())
                        .collect(Collectors.toSet()));
            }
        } catch (Exception e) {
            log.error("Error fetching carousel children for id {}: {}", carouselId, e.getMessage());
        }

        return mediaUrls;
    }

    /*
        * 인스타그램 단일 미디어 조회하기
        * @param member
        * @param postingId
        * @return Data
     */
    public InstagramMediaResponse.Data getMediaById(String postingId, Member member) {
        String accessToken = getAccessToken(member);
        String url = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v22.0/" + postingId)
                .queryParam("fields", "id,caption,media_type,media_url,permalink,timestamp,children{media_url}")
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUriString();

        try {
            String rawResponse = restClient.get()
                    .uri(URI.create(url))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode mediaNode = objectMapper.readTree(rawResponse);
            return buildMediaData(mediaNode, accessToken);

        } catch (Exception e) {
            log.error("Failed to fetch Instagram media by ID {}: {}", postingId, e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /**
     * 인스타그램 미디어 데이터 빌드
     * @param mediaNode
     * @param accessToken
     * @return InstagramMediaResponse.Data
     */
    private InstagramMediaResponse.Data buildMediaData(JsonNode mediaNode, String accessToken) {
        String id = mediaNode.get("id").asText();
        String caption = mediaNode.hasNonNull("caption") ? mediaNode.get("caption").asText() : null;
        String mediaType = mediaNode.get("media_type").asText();
        String permalink = mediaNode.get("permalink").asText();
        String timestamp = mediaNode.hasNonNull("timestamp") ? mediaNode.get("timestamp").asText() : null;

        List<String> mediaUrls;

        if ("CAROUSEL_ALBUM".equals(mediaType)) {
            // children 직접 포함되어 있으면
            if (mediaNode.has("children")) {
                JsonNode children = mediaNode.get("children").get("data");
                mediaUrls = StreamSupport.stream(children.spliterator(), false)
                        .map(node -> node.get("media_url").asText())
                        .collect(Collectors.toList());
            } else {
                // 아니면 carouselId를 통해 children API 호출
                mediaUrls = new ArrayList<>(getCarouselChildren(id, accessToken));
            }
        } else {
            mediaUrls = mediaNode.hasNonNull("media_url") ? List.of(mediaNode.get("media_url").asText()) : Collections.emptyList();
        }

        return InstagramMediaResponse.Data.builder()
                .id(id)
                .caption(caption)
                .media_type(mediaType)
                .media_urls(mediaUrls)
                .permalink(permalink)
                .timestamp(timestamp)
                .build();
    }
}