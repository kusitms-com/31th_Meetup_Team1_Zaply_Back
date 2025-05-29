package org.zapply.product.global.scheduler.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UpdateScheduleEvent {
    private final Long jobId;
    private final Long postingId;
    private final LocalDateTime newExecuteAt;
    private final Runnable action;
}
