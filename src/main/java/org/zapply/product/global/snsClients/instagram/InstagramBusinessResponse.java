package org.zapply.product.global.snsClients.instagram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InstagramBusinessResponse(
        List<PageData> data
) {
    public record PageData(
            String id,
            String name,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("instagram_business_account") InstagramBusinessAccount instagramBusinessAccount
    ) {}

    public record InstagramBusinessAccount(
            String id
    ) {}
}