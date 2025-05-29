package org.zapply.product.global.snsClients.instagram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InstagramToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int expiresIn
) {}