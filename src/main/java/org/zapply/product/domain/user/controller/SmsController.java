package org.zapply.product.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zapply.product.domain.user.dto.request.CertificateRequest;
import org.zapply.product.domain.user.dto.request.SmsRequest;
import org.zapply.product.domain.user.service.SmsService;
import org.zapply.product.global.apiPayload.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sms")
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public ApiResponse<?> SendSMS(@RequestBody @Valid SmsRequest smsRequest){
        smsService.SendSms(smsRequest);
        return ApiResponse.success();
    }

    @PostMapping("/certification")
    public ApiResponse<String> certification(@RequestBody @Valid CertificateRequest certificateRequest){
        return ApiResponse.success(smsService.certificate(certificateRequest));
    }
}
