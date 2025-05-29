package org.zapply.product.domain.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.posting.repository.PostingRepository;
import org.zapply.product.domain.project.dto.response.ProjectInfoResponse;
import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.domain.project.repository.ProjectRepository;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PostingRepository postingRepository;

    @Transactional
    public Long createEmptyProject(Member member) {
        Project project = Project.builder()
                .member(member)
                .projectTitle("")       // 빈 타이틀
                .build();

        return projectRepository.save(project).getProjectId();
    }


    public List<ProjectInfoResponse> getProjects(Member member) {

        List<Project> projects = projectRepository.findAllByMemberAndDeletedAtIsNull(member);

        return projects.stream()
                .map(project -> {
                    LocalDateTime earliest = postingRepository.findEarliestScheduledAtByProject(project);
                    List<SNSType> types = postingRepository.findDistinctTypesByProject(project);
                    return new ProjectInfoResponse(
                            project.getProjectId(),
                            project.getProjectTitle(),
                            project.getProjectThumbnail(),
                            earliest,
                            types
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProject(Member member, Long projectId) {
        Project project = projectRepository.findByProjectIdAndMemberAndDeletedAtIsNull(projectId, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.PROJECT_NOT_FOUND));

        projectRepository.delete(project);
    }
}
