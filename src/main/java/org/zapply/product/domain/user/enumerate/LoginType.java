package org.zapply.product.domain.user.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {
    GOOGLE("구글 회원가입"),
    DEFAULT("일반 회원가입");

    private final String description;
}
