package org.zapply.product.global.snsClients.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThreadsMediaClient {

    @Value("${base-url.threads}")
    private String THREADS_API_BASE;

    private final RestClient restClient = RestClient.create();

    /**
     * 스레드 미디어 조회하기 (모든 미디어)
     * @param accessToken
     * @return
     */
    public ThreadsMediaResponse getAllThreadsMedia(String accessToken, String cursor, int size) {
        long nowTimestamp = System.currentTimeMillis() / 1000;

        URI uri = UriComponentsBuilder
                .fromHttpUrl(THREADS_API_BASE + "/me/threads")
                .queryParam("fields", "id,media_product_type,media_type,permalink,owner,username,text,timestamp,shortcode,thumbnail_url,is_quote_post,children,alt_text")
                .queryParam("since", "2023-07-06")
                .queryParam("until", nowTimestamp)
                .queryParam("limit", size)
                .queryParam("access_token", accessToken)
                .queryParamIfPresent("after",
                        Optional.ofNullable(cursor).filter(s -> !s.isBlank()))
                .build()
                .encode()
                .toUri();
        try {
            ThreadsMediaResponse response = Objects.requireNonNull(restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ThreadsMediaResponse.class));

            List<ThreadsMediaResponse.ThreadsMedia> processed = response.data().stream()
                    .filter(media -> !media.is_quote_post())
                    .filter(media -> "IMAGE".equals(media.media_type()) ||
                            "CAROUSEL_ALBUM".equals(media.media_type()) ||
                            "TEXT_POST".equals(media.media_type()))
                    .map(media -> processMedia(media, accessToken))
                    .filter(Objects::nonNull)
                    .toList();

            return new ThreadsMediaResponse(processed, response.paging());

        } catch (Exception e) {
            log.error("Error fetching Threads media: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_MEDIA_NOT_FOUND);
        }
    }

    /**
     * 스레드 미디어 조회하기 (단일 미디어)
     * @param accessToken
     * @param mediaId
     * @return
     */
    public ThreadsMediaResponse.ThreadsMedia getSingleThreadsMedia(String accessToken, String mediaId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(THREADS_API_BASE + "/" + mediaId)
                .queryParam("fields", "id,media_product_type,media_type,permalink,owner,username,text,timestamp,shortcode,thumbnail_url,is_quote_post,children,alt_text")
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUri();

        ThreadsMediaResponse.ThreadsMedia media;
        try {
            media = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ThreadsMediaResponse.ThreadsMedia.class);
        } catch (Exception e) {
            log.error("Error fetching Threads media by ID {}: {}", mediaId, e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_MEDIA_NOT_FOUND);
        }

        if (media == null || media.is_quote_post() ||
                !("IMAGE".equals(media.media_type()) || "CAROUSEL_ALBUM".equals(media.media_type())
                        || "TEXT_POST".equals(media.media_type()))) {
            throw new CoreException(GlobalErrorType.THREADS_MEDIA_NOT_FOUND);
        }

        return processMedia(media, accessToken);
    }

    /**
     * 스레드 미디어 처리하기
     * @param media
     * @param accessToken
     * @return
     */
    private ThreadsMediaResponse.ThreadsMedia processMedia(ThreadsMediaResponse.ThreadsMedia media, String accessToken) {
        String mediaId = media.id();
        String mediaType = media.media_type();

        if ("IMAGE".equals(mediaType)) {
            String mediaUrl = fetchMediaUrl(mediaId, accessToken);
            if (mediaUrl == null) {
                throw new CoreException(GlobalErrorType.THREADS_MEDIA_NOT_FOUND);
            }
            return media
                    .withMediaUrl(mediaUrl)
                    .withCarouselMediaUrls(List.of());
        }

        if ("CAROUSEL_ALBUM".equals(mediaType) && media.children() != null && !media.children().data().isEmpty()) {
            List<String> childMediaIds = media.children().data().stream()
                    .map(ThreadsMediaResponse.ThreadsMedia.Child::id)
                    .filter(Objects::nonNull)
                    .toList();

            List<String> imageUrls = new ArrayList<>();
            for (String childId : childMediaIds) {
                String childMediaUrl = fetchMediaUrl(childId, accessToken);
                if (childMediaUrl != null) {
                    imageUrls.add(childMediaUrl);
                }
            }

            if (imageUrls.isEmpty()) {
                throw new CoreException(GlobalErrorType.THREADS_MEDIA_NOT_FOUND);
            }

            return media
                    .withMediaUrl("")
                    .withCarouselMediaUrls(imageUrls);
        }
        if ("TEXT_POST".equals(mediaType)) {
            return media
                    .withMediaUrl("")
                    .withCarouselMediaUrls(List.of());
        }

        return null;
    }

    /**
     * 스레드 미디어 URL 가져오기
     * @param mediaId
     * @param accessToken
     * @return
     */
    private String fetchMediaUrl(String mediaId, String accessToken) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(THREADS_API_BASE + "/" + mediaId)
                .queryParam("fields", "media_url,media_type")
                .queryParam("access_token", accessToken)
                .build()
                .encode()
                .toUri();

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(java.util.Map.class);
            if (response != null && "IMAGE".equals(response.get("media_type"))) {
                return String.valueOf(response.get("media_url"));
            }
        } catch (Exception e) {
            log.error("Error fetching media URL for Threads media ID {}: {}", mediaId, e.getMessage());
            throw new CoreException(GlobalErrorType.THREADS_MEDIA_NOT_FOUND);
        }
        return null;
    }
}