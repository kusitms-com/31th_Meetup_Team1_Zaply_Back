package org.zapply.product.domain.posting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.zapply.product.domain.posting.dto.request.ToneTransferRequest;
import org.zapply.product.domain.posting.dto.response.ToneTransferResponse;
import org.zapply.product.domain.posting.service.ClovaService;
import org.zapply.product.global.apiPayload.response.ApiResponse;

@RestController
@RequestMapping("/v1/posting")
@RequiredArgsConstructor
@Tag(name = "Posting", description = "게시글 발행 API")
public class ClovaController {

    private final ClovaService clovaService;

    @PostMapping("/transfer")
    @Operation(summary = "SNS 글 분위기 변환", description = "입력된 콘텐츠를 지정된 SNS 타입에 맞춰 변환합니다.")
    public ApiResponse<ToneTransferResponse> transferToSNSTone(@Valid @RequestBody ToneTransferRequest toneTransferRequest) {
        return ApiResponse.success(clovaService.TransferToSNSTone(toneTransferRequest));
    }

    @PostMapping("/title")
    @Operation(summary = "글에 맞는 컨텐츠 제목 추천", description = "하나의 게시글을 입력받고 전체 컨텐츠의 제목을 추천해줍니다.")
    public ApiResponse<String> recommendProjectTitle(@Valid @RequestBody ToneTransferRequest toneTransferRequest) {
        return ApiResponse.success(clovaService.recommendProjectTitle(toneTransferRequest));
    }

}
