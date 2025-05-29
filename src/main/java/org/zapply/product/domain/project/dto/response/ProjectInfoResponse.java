package org.zapply.product.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectInfoResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,

        @Schema(description = "프로젝트 제목", example = "제목")
        String projectTitle,

        @Schema(description = "프로젝트 썸네일", example = "https://example.com/thumbnail.jpg")
        String projectThumbnail,

        @Schema(description = "가장 빠른 스케줄링 시간", example = "2023-10-01T12:00:00")
        LocalDateTime earliestScheduledAt,

        @Schema(description = "게시물 타입", example = "[\"FACEBOOK\", \"INSTAGRAM\"]")
        List<SNSType> postingTypes
) {}
