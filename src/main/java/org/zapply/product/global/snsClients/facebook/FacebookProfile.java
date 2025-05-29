package org.zapply.product.global.snsClients.facebook;

public record FacebookProfile(
        String id,
        String name,
        String email,
        Picture picture
) {
    public record Picture(
            Data data
    ) {
        public record Data(
                String url
        ) {}
    }
}