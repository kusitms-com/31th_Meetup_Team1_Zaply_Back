package org.zapply.product.global.scheduler.task;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.zapply.product.global.scheduler.entity.ScheduledJob;
import org.zapply.product.global.scheduler.enumerate.JobStatus;
import org.zapply.product.global.scheduler.repository.ScheduledJobRepository;
import org.zapply.product.global.scheduler.service.SchedulingService;

import java.util.List;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TaskSchedulerEventHandler {
    private final ScheduledJobRepository jobRepo;
    private final SchedulingService schedulingService;

    // 애플리케이션 시작 시 예약된 작업을 재등록합니다. -> 추후 수정 예정
    @EventListener(ApplicationReadyEvent.class)
    public void reRegisterJobs() {
        List<ScheduledJob> pending = jobRepo.findAllByStatus(JobStatus.SCHEDULED);
        for (ScheduledJob job : pending) {
            long id = job.getId();
            LocalDateTime execAt = job.getExecuteAt();
            schedulingService.scheduleTask(job.getPostingId(), execAt, () -> {
                // TODO: 실제 작업 호출 -> 이부분은 페북까지 나오면 수정할게요
            });
        }
    }

    @EventListener
    public void handleUpdateSchedule(UpdateScheduleEvent event) {
        schedulingService.cancelTask(event.getJobId());
        schedulingService.scheduleTask(
                event.getPostingId(),
                event.getNewExecuteAt(),
                event.getAction()
        );
    }
}

