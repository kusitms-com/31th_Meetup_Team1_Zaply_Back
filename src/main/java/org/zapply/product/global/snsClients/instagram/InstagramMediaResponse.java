package org.zapply.product.global.snsClients.instagram;

import lombok.Builder;

import java.util.List;

@Builder
public record InstagramMediaResponse(
        Data data,
        Paging paging
) {
    @Builder
    public record Data(
            String id,
            String caption,
            String media_type,
            List<String> media_urls,
            String permalink,
            String timestamp
    ) {
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
