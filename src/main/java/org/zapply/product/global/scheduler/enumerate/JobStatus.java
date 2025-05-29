package org.zapply.product.global.scheduler.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobStatus {
    SCHEDULED("예약됨"),
    COMPLETED("발행됨"),
    FAILED("실패함"),
    CANCELED("취소됨");

    private final String description;
}

