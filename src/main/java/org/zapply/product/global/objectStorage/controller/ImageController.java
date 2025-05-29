package org.zapply.product.global.objectStorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zapply.product.global.objectStorage.service.ObjectStorageService;
import org.zapply.product.global.objectStorage.dto.ReadPreSignedUrlResponse;

@RestController
@RequestMapping("/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final ObjectStorageService storageService;

    @GetMapping("/presigned-url")
    public ReadPreSignedUrlResponse presignedUrl(@RequestParam String prefix, @RequestParam String fileName) {
        return storageService.getPreSignedUrl(prefix, fileName);
    }
}
