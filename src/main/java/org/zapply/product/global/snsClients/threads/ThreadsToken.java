package org.zapply.product.global.snsClients.threads;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThreadsToken(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn
) {}