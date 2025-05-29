package org.zapply.product.domain.posting.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostingState {
    PENDING("작성 중"),
    POSTED("발행 완료"),
    SCHEDULED("발행 예약");

    private final String description;
}
