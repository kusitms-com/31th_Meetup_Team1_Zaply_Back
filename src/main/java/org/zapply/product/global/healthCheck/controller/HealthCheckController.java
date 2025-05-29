package org.zapply.product.global.healthCheck.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zapply.product.global.objectStorage.service.ObjectStorageService;
import org.zapply.product.global.objectStorage.dto.ReadPreSignedUrlResponse;

@RestController
@RequestMapping("/v1/healthcheck")
@RequiredArgsConstructor
public class HealthCheckController {

    private final ObjectStorageService storageService;

    @GetMapping
    public ResponseEntity<String> healthcheck() {
        // 필요한 최소 검사(예: DB 커넥션 확인 등)를 직접 넣어도 되고
        return ResponseEntity.ok("OK");
    }
}
