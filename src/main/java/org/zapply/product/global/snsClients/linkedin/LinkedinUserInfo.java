package org.zapply.product.global.snsClients.linkedin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record LinkedinUserInfo(
        @JsonProperty("sub") String sub,
        @JsonProperty("email_verified") boolean emailVerified,
        @JsonProperty("name") String name,
        @JsonProperty("locale") LinkedinLocale locale,
        @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName,
        @JsonProperty("email") String email
) {
    /**
     * Unwrapped nested record for locale information
     */
    public record LinkedinLocale(
            @JsonProperty("country") String country,
            @JsonProperty("language") String language
    ) {}
}
