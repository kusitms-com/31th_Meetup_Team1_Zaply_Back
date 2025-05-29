package org.zapply.product.global.snsClients.facebook;

import lombok.Builder;

import java.util.List;

@Builder
public record FacebookMediaResponse(
        Data data,
        Paging paging
) {
    @Builder
    public record Data(
            String id,
            String message,
            String createdTime,
            String media_type,
            String permalinkUrl,
            List<String> imageUrls
    ) {
        public Data withImageUrls(List<String> imageUrls) {
            return Data.builder()
                    .id(this.id)
                    .message(this.message)
                    .createdTime(this.createdTime)
                    .media_type(this.media_type)
                    .permalinkUrl(this.permalinkUrl)
                    .imageUrls(imageUrls)
                    .build();
        }
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