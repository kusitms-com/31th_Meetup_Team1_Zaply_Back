package org.zapply.product.global.snsClients.threads;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ThreadsProfile(
        String id,

        String username,

        String name,

        @JsonProperty("threads_profile_picture_url")
        String profilePictureUrl,

        @JsonProperty("threads_biography")
        String biography
) {}