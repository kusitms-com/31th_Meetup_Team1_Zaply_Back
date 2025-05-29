package org.zapply.product.global.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.zapply.product.global.scheduler.entity.ScheduledJob;
import org.zapply.product.global.scheduler.enumerate.JobStatus;
import org.zapply.product.global.scheduler.repository.ScheduledJobRepository;
import org.zapply.product.global.scheduler.repository.SchedulingRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedulingServiceTest {

    @Mock private ScheduledJobRepository jobRepo;
    @Mock private SchedulingRepository schedulingRepo;
    @Mock private ThreadPoolTaskScheduler taskScheduler;
    @Mock private TransactionTemplate txTemplate;
    @InjectMocks private SchedulingService service;
    @Mock private ScheduledFuture<?> future;

    @BeforeEach
    void setUp() {
        // Assign ID on save
        when(jobRepo.save(any(ScheduledJob.class))).thenAnswer(inv -> {
            ScheduledJob job = inv.getArgument(0);
            if (job.getId() == null) ReflectionTestUtils.setField(job, "id", 1L);
            return job;
        });
        // Execute transaction immediately
        when(txTemplate.execute(any())).thenAnswer(inv -> {
            TransactionCallback<?> cb = inv.getArgument(0);
            return cb.doInTransaction(null);
        });
    }

    @Test
    void scheduleTask_runsActionAndMarksCompleted() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Runnable action = () -> ran.set(true);
        LocalDateTime execAt = LocalDateTime.now();

        // schedule stub: run action immediately
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return future;
        }).when(taskScheduler).schedule(any(Runnable.class), any(java.util.Date.class));
        doNothing().when(schedulingRepo).add(1L, future);

        Long jobId = service.scheduleTask(42L, execAt, action);

        assertThat(ran).isTrue();
        // verify status update
        verify(jobRepo, atLeastOnce()).save(argThat(j -> j.getStatus() == JobStatus.COMPLETED));
    }

    @Test
    void cancelTask_cancelsFutureAndMarksCanceled() {
        ScheduledJob job = ScheduledJob.builder()
                .postingId(99L)
                .executeAt(LocalDateTime.now())
                .status(JobStatus.SCHEDULED)
                .build();
        ReflectionTestUtils.setField(job, "id", 2L);

        when(jobRepo.findByPostingIdAndStatus(99L, JobStatus.SCHEDULED))
                .thenReturn(Optional.of(job));
        doReturn(future).when(schedulingRepo).remove(2L);
        doReturn(true).when(future).cancel(false);

        service.cancelTask(99L);

        verify(future).cancel(false);
        verify(jobRepo).save(argThat(j -> j.getStatus() == JobStatus.CANCELED));
    }
}
