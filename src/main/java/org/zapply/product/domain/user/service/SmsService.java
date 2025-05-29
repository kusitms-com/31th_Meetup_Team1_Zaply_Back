package org.zapply.product.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.user.dto.request.CertificateRequest;
import org.zapply.product.domain.user.dto.request.SmsRequest;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.coolSMS.SMSClient;
import org.zapply.product.global.redis.RedisClient;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class SmsService{

    private final SMSClient smsClient;
    private final RedisClient redisClient;


    private static final long OTP_EXPIRATION_MILLIS = 5 * 60_000L;
    private static final SecureRandom secureRandom = new SecureRandom();


    public void SendSms(SmsRequest smsRequestDto) {
        String phoneNum = smsRequestDto.phoneNum();

        String certificationCode = String.format("%06d", secureRandom.nextInt(1000000));

        smsClient.sendSMS(phoneNum, certificationCode);
        redisClient.setValue(phoneNum, certificationCode,OTP_EXPIRATION_MILLIS);
    }

    public String certificate(CertificateRequest certificateRequest) {
        String phoneNum = certificateRequest.phoneNum();
        if (redisClient.getValue(phoneNum).equals(certificateRequest.authNum())){
            redisClient.deleteValue(phoneNum);
            return "휴대폰 인증 성공";
        }
        else{
            throw new CoreException(GlobalErrorType.SMS_UNAUTHENTICATED);
        }
    }
}
