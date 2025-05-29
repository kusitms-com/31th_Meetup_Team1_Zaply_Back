package org.zapply.product.domain.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zapply.product.domain.project.dto.response.ProjectInfoResponse;
import org.zapply.product.domain.project.service.ProjectService;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import org.zapply.product.global.security.AuthDetails;

import java.util.List;

@RestController
@RequestMapping("/v1/project")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 API")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    @Operation(summary = "빈 프로젝트 생성하기", description = "(SNS 발행) 프로젝트 생성 시에 빈 프로젝트 생성하기")
    public ApiResponse<?> createEmptyProject(@AuthenticationPrincipal AuthDetails authDetails) {
        return ApiResponse.success(projectService.createEmptyProject(authDetails.getMember()));
    }

    @GetMapping
    @Operation(summary = "내 프로젝트 조회하기", description = "내가 소유한 프로젝트를 조회한다")
    public ApiResponse<List<ProjectInfoResponse>> getUserProjects(@AuthenticationPrincipal AuthDetails authDetails) {
        return ApiResponse.success(projectService.getProjects(authDetails.getMember()));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제하기", description = "프로젝트를 삭제한다")
    public ApiResponse<?> deleteProject(@AuthenticationPrincipal AuthDetails authDetails,
                                        @PathVariable Long projectId) {
        projectService.deleteProject(authDetails.getMember(), projectId);
        return ApiResponse.success();
    }
}
