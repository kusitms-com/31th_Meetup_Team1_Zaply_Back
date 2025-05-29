package org.zapply.product.domain.user.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record SmsRequest(
        @NotEmpty(message = "휴대폰 번호를 입력해주세요")
        String phoneNum
) { }