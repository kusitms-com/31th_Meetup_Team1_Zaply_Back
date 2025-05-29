package org.zapply.product.global.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zapply.product.global.scheduler.entity.ScheduledJob;
import org.zapply.product.global.scheduler.enumerate.JobStatus;

import java.util.List;
import java.util.Optional;

public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    List<ScheduledJob> findAllByStatus(JobStatus status);
    Optional<ScheduledJob> findByPostingIdAndStatus(Long jobId, JobStatus status);
}
