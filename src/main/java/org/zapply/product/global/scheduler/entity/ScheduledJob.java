package org.zapply.product.global.scheduler.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.zapply.product.global.scheduler.enumerate.JobStatus;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduledJob {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private LocalDateTime executeAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @NotNull
    private Long postingId;

    @Builder
    public ScheduledJob(LocalDateTime executeAt, JobStatus status, Long postingId) {
        this.executeAt = executeAt;
        this.status = status;
        this.postingId = postingId;
    }

    public void updateStatus(JobStatus status) {
        this.status = status;
    }
}

