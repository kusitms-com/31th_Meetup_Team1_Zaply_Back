package org.zapply.product.global.snsClients.linkedin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkedinToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn
) {}