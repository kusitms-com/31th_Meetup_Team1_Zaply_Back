package org.zapply.product.global.snsClients.threads;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public record ThreadsMediaResponse(
        @Schema(description = "스레드 미디어 정보")
        List<ThreadsMedia> data,
        @Schema(description = "스레드 미디어 페이징 정보")
        Paging paging
) {
    @Builder
    public record ThreadsMedia(
            @Schema(description = "미디어 ID")
            String id,

            @Schema(description = "미디어 제품 타입", example = "THREADS")
            String media_product_type,

            @Schema(description = "미디어 타입", example = "TEXT || IMAGE || CAROUSEL_ALBUM")
            String media_type,

            @Schema(description = "미디어 URL")
            String permalink,

            @Schema(description = "미디어 소유자 정보")
            Owner owner,

            @Schema(description = "미디어 소유자 이름")
            String username,

            @Schema(description = "미디어 텍스트")
            String text,

            @Schema(description = "미디어 타임스탬프")
            String timestamp,

            @Schema(description = "미디어 썸네일 URL")
            String thumbnail_url,

            @Schema(description = "미디어 쇼트코드")
            String shortcode,

            @Schema(description = "미디어가 인용된 게시물 여부")
            boolean is_quote_post,

            @Schema(description = "미디어 자식 정보")
            Children children,

            @Schema(description = "미디어 대체 텍스트")
            String alt_text,

            @Schema(description = "미디어 URL")
            String media_url,

            @Schema(description = "미디어 캐러셀 URL")
            List<String> carousel_media_urls,

            @Schema(description = "인용된 게시물")
            String quoted_post,

            @Schema(description = "재게시된 게시물")
            String reposted_post,

            @Schema(description = "링크 첨부 URL")
            String link_attachment_url,

            @Schema(description = "GIF URL")
            String gif_url
    ) {
        public ThreadsMedia withMediaUrl(String mediaUrl) {
            return getThreadsMedia(mediaUrl, carousel_media_urls);
        }

        private ThreadsMedia getThreadsMedia(String mediaUrl, List<String> carouselMediaUrls) {
            return ThreadsMedia.builder()
                    .id(id)
                    .media_product_type(media_product_type)
                    .media_type(media_type)
                    .permalink(permalink)
                    .owner(owner)
                    .username(username)
                    .text(text)
                    .timestamp(timestamp)
                    .thumbnail_url(thumbnail_url)
                    .shortcode(shortcode)
                    .is_quote_post(is_quote_post)
                    .children(children)
                    .alt_text(alt_text)
                    .media_url(mediaUrl)
                    .carousel_media_urls(carouselMediaUrls)
                    .quoted_post(quoted_post)
                    .reposted_post(reposted_post)
                    .link_attachment_url(link_attachment_url)
                    .gif_url(gif_url)
                    .build();
        }

        public ThreadsMedia withCarouselMediaUrls(List<String> carouselMediaUrls) {
            return getThreadsMedia(media_url, carouselMediaUrls);
        }

        public record Children(
                List<Child> data
        ) {}

        public record Child(
                String id
        ) {}

        public record Owner(
                String id
        ) {}
    }

    public record Paging(
            Cursors cursors
    ) {
        public record Cursors(
                String before,
                String after
        ) {}
    }
}