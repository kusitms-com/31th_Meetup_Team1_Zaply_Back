package org.zapply.product.global.snsClients.facebook;

public record FacebookPage(
        String id,
        String name,
        String accessToken,
        String category
) {}