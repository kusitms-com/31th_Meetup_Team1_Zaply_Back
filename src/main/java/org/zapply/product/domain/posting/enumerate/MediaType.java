package org.zapply.product.domain.posting.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    IMAGE("IMAGE"),
    TEXT("TEXT"),
    VIDEO("VIDEO");

    private final String description;
}
