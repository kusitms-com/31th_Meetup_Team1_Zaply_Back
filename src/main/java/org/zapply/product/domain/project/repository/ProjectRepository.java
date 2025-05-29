package org.zapply.product.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.domain.user.entity.Member;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByMemberAndDeletedAtIsNull(Member member);
    Optional<Project> findByProjectIdAndDeletedAtIsNull(Long projectId);
    boolean existsByProjectIdAndMember_Id(Long projectId, Long memberId);
    Optional<Project> findByProjectIdAndMemberAndDeletedAtIsNull(Long projectId, Member member);
}
