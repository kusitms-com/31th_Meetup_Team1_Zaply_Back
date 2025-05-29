package org.zapply.product.domain.posting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostingRepository extends JpaRepository<Posting, Long> {
    List<Posting> findAllByProject_ProjectIdAndDeletedAtIsNull(Long projectId);
    List<Posting> findAllByProjectAndDeletedAtIsNull(Project project);

    Optional<Posting> findByPostingIdAndPostingStateAndDeletedAtIsNull(Long postingId, PostingState postingState);
    // 프로젝트별 가장 빠른 scheduledAt
    @Query("select min(p.scheduledAt) from Posting p where p.project = :project and p.deletedAt is null")
    LocalDateTime findEarliestScheduledAtByProject(@Param("project") Project project);

    // distinct postingType
    @Query("select distinct p.postingType from Posting p where p.project = :project and p.deletedAt is null")
    List<SNSType> findDistinctTypesByProject(@Param("project") Project project);
}
