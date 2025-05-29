package org.zapply.product.global.snsClients.threads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Threads 인사이트 API 응답 DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ThreadsInsightResponse(
        @Schema(description = "인사이트 데이터 리스트")
        List<InsightData> data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InsightData(
            @Schema(description = "인사이트 이름", example = "likes")
            String name,

            @Schema(description = "타이틀", example = "좋아요")
            String title,

            @Schema(description = "값 리스트")
            List<Value> values,

            @Schema(description = "설명", example = "게시물에 대한 좋아요 수입니다.")
            String description
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(
            @Schema(description = "값", example = "100")
            @JsonProperty("value")
            Long value
    ) {
    }
}