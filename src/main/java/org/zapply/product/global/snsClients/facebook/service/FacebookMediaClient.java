package org.zapply.product.global.snsClients.facebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.zapply.product.global.snsClients.facebook.FacebookMediaResponse;
import org.zapply.product.global.snsClients.facebook.FacebookPage;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookMediaClient {

    private static final String FB_GRAPH_BASE = "https://graph.facebook.com/v22.0";
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;

    public Account getFacebookAccount(Member member) {
        return accountRepository.findByAccountTypeAndMember(SNSType.FACEBOOK, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_NOT_FOUND));
    }
    public List<FacebookMediaResponse.Data> getAllPagePosts(String userAccessToken) {
        List<FacebookMediaResponse.Data> allPosts = new ArrayList<>();
        List<FacebookPage> managedPages = getManagedPages(userAccessToken);

        for (FacebookPage page : managedPages) {
            String pageId = page.id();
            String pageAccessToken = page.accessToken();
            String url = String.format(
                    "https://graph.facebook.com/v22.0/%s/posts?fields=id,message,created_time,permalink_url,attachments&access_token=%s",
                    pageId, pageAccessToken
            );

            log.info("Fetching posts for page: {}, URL: {}", pageId, url);
            while (url != null) {
                try {
                    String rawResponse = restClient.get()
                            .uri(URI.create(url))
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .body(String.class);

                    log.info("Raw response for page {}: {}", pageId, rawResponse);
                    JsonNode root = objectMapper.readTree(rawResponse);
                    JsonNode dataArray = root.get("data");

                    if (dataArray != null && dataArray.isArray()) {
                        for (JsonNode postNode : dataArray) {
                            FacebookMediaResponse.Data post = buildPostData(postNode);

                            // 이미지 URL 리스트 추출
                            Set<String> imageUrls = new LinkedHashSet<>();
                            JsonNode attachments = postNode.path("attachments").path("data");

                            if (attachments.isArray()) {
                                for (JsonNode attachment : attachments) {
                                    // 기본 이미지
                                    JsonNode mediaImage = attachment.path("media").path("image").path("src");
                                    if (!mediaImage.isMissingNode()) {
                                        imageUrls.add(mediaImage.asText());
                                    }

                                    // subattachments 이미지들
                                    JsonNode subattachments = attachment.path("subattachments").path("data");
                                    if (subattachments.isArray()) {
                                        for (JsonNode sub : subattachments) {
                                            JsonNode subMediaImage = sub.path("media").path("image").path("src");
                                            if (!subMediaImage.isMissingNode()) {
                                                imageUrls.add(subMediaImage.asText());
                                            }
                                        }
                                    }
                                }
                            }

                            post = post.withImageUrls(new ArrayList<>(imageUrls));
                            allPosts.add(post);
                        }
                    }

                    JsonNode paging = root.get("paging");
                    url = (paging != null && paging.has("next")) ? paging.get("next").asText() : null;

                } catch (Exception e) {
                    log.error("Facebook API Error while fetching posts for page {}: {}", pageId, e.getMessage());
                    throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
                }
            }
        }

        log.info("Total posts fetched: {}", allPosts.size());
        return allPosts;
    }

    public FacebookMediaResponse.Data getPostById(String postId, String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(FB_GRAPH_BASE + "/" + postId)
                .queryParam("fields", "id,message,created_time,permalink_url,attachments")
                .queryParam("access_token", accessToken)
                .build().encode().toUriString();

        try {
            String rawResponse = restClient.get()
                    .uri(URI.create(url))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            log.info("Raw response for post {}: {}", postId, rawResponse);
            JsonNode postNode = objectMapper.readTree(rawResponse);
            return buildPostData(postNode);

        } catch (Exception e) {
            log.error("Failed to fetch Facebook post by ID {}: {}", postId, e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    public String getPageId(String accessToken) {
        String url = FB_GRAPH_BASE + "/me?access_token=" + accessToken;

        try {
            String response = restClient.get()
                    .uri(URI.create(url))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("id").asText();

        } catch (Exception e) {
            log.error("Error fetching Facebook page ID: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }

    public List<FacebookPage> getManagedPages(String userAccessToken) {
        String url = FB_GRAPH_BASE + "/me/accounts?access_token=" + userAccessToken;
        try {
            String response = restClient.get()
                    .uri(URI.create(url))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);

            log.info("Received response for managed pages: {}", response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode dataArray = root.get("data");

            if (dataArray != null && dataArray.isArray()) {
                List<FacebookPage> pages = new ArrayList<>();
                for (JsonNode node : dataArray) {
                    FacebookPage page = new FacebookPage(
                            node.get("id").asText(),
                            node.get("name").asText(),
                            node.has("access_token") ? node.get("access_token").asText() : null,
                            node.has("category") ? node.get("category").asText() : null
                    );
                    pages.add(page);
                }
                return pages;
            } else {
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error fetching managed pages: {}", e.getMessage(), e);
            throw new CoreException(GlobalErrorType.FACEBOOK_API_ERROR);
        }
    }
    private List<String> extractImageUrls(JsonNode attachmentsNode) {
        Set<String> imageUrlSet = new LinkedHashSet<>();

        if (attachmentsNode.isArray()) {
            for (JsonNode attachment : attachmentsNode) {
                // 기본 이미지
                JsonNode imageNode = attachment.path("media").path("image").path("src");
                if (!imageNode.isMissingNode()) {
                    imageUrlSet.add(imageNode.asText());
                }
                // 서브어태치먼트(앨범 등)
                JsonNode subAttachments = attachment.path("subattachments").path("data");
                if (subAttachments.isArray() && subAttachments.size() > 0) {
                    imageUrlSet.addAll(extractImageUrlsToSet(subAttachments));
                }
            }
        }
        return new ArrayList<>(imageUrlSet);
    }

    // 재귀 호출용, Set 반환
    private Set<String> extractImageUrlsToSet(JsonNode attachmentsNode) {
        Set<String> imageUrlSet = new LinkedHashSet<>();

        if (attachmentsNode.isArray()) {
            for (JsonNode attachment : attachmentsNode) {
                JsonNode imageNode = attachment.path("media").path("image").path("src");
                if (!imageNode.isMissingNode()) {
                    imageUrlSet.add(imageNode.asText());
                }
                JsonNode subAttachments = attachment.path("subattachments").path("data");
                if (subAttachments.isArray() && subAttachments.size() > 0) {
                    imageUrlSet.addAll(extractImageUrlsToSet(subAttachments));
                }
            }
        }
        return imageUrlSet;
    }

    private FacebookMediaResponse.Data buildPostData(JsonNode postNode) {
        String id = postNode.path("id").asText();
        String message = postNode.path("message").asText(null);
        String createdTime = postNode.path("created_time").asText(null);
        String permalinkUrl = postNode.path("permalink_url").asText(null);
        String mediaType = "IMAGE";

        JsonNode attachmentsNode = postNode.path("attachments").path("data");
        List<String> imageUrls = extractImageUrls(attachmentsNode);

        return FacebookMediaResponse.Data.builder()
                .id(id)
                .message(message)
                .createdTime(createdTime)
                .media_type(mediaType)
                .permalinkUrl(permalinkUrl)
                .imageUrls(imageUrls)
                .build();
    }

}