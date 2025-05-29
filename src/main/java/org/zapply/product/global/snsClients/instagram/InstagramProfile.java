package org.zapply.product.global.snsClients.instagram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InstagramProfile(
        String id,
        String username,
        String name,
        @JsonProperty("profile_picture_url") String profilePictureUrl
) {}