package org.zapply.product.global.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.scheduler.entity.ScheduledJob;
import org.zapply.product.global.scheduler.enumerate.JobStatus;
import org.zapply.product.global.scheduler.repository.ScheduledJobRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.support.TransactionTemplate;
import org.zapply.product.global.scheduler.repository.SchedulingRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final ScheduledJobRepository jobRepo;
    private final SchedulingRepository schedulingRepo;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final TransactionTemplate txTemplate;
    private final ZoneId zone = ZoneId.systemDefault();

    public Long scheduleTask(Long postingId, LocalDateTime execAt, Runnable action) {
        ScheduledJob job = ScheduledJob.builder()
                .postingId(postingId)
                .executeAt(execAt)
                .status(JobStatus.SCHEDULED)
                .build();
        jobRepo.save(job);

        Date runAt = Date.from(execAt.atZone(zone).toInstant());
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            txTemplate.execute(status -> {
                try {
                    action.run();
                    job.updateStatus(JobStatus.COMPLETED);
                } catch (Exception e) {
                    job.updateStatus(JobStatus.FAILED);
                }
                jobRepo.save(job);
                return null;
            });
            schedulingRepo.remove(job.getId());
        }, runAt);

        schedulingRepo.add(job.getId(), future);
        return job.getId();
    }

    public void cancelTask(Long postingId) {
        ScheduledJob job = jobRepo.findByPostingIdAndStatus(postingId, JobStatus.SCHEDULED)
                .orElseThrow(() -> new CoreException(GlobalErrorType.SCHEDULED_JOB_NOT_FOUND));

        Long jobId = job.getId();
        ScheduledFuture<?> future = schedulingRepo.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }
        job.updateStatus(JobStatus.CANCELED);
        jobRepo.save(job);
    }
}

