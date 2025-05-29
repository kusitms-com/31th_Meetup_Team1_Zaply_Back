package org.zapply.product.domain.user.dto.request;

public record MemberRequest(
        String name,
        String password
) {
}
