package org.zapply.product.global.objectStorage.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zapply.product.global.objectStorage.dto.ReadPreSignedUrlResponse;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final AmazonS3 ncpS3Client;

    @Value("${cloud.ncp.object-storage.bucket-name}")
    private String bucket;

    public ReadPreSignedUrlResponse getPreSignedUrl(String prefix, String originalFileName) {
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);

        String key = String.format("%s/%s-%s", prefix, UUID.randomUUID(), encodedFileName);

        Date expiration = new Date(System.currentTimeMillis() + 60 * 1000);

        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        req.addRequestParameter(Headers.S3_CANNED_ACL,
                CannedAccessControlList.PublicRead.toString());

        URL presignedUrl = ncpS3Client.generatePresignedUrl(req);
        String objectUrl = ncpS3Client.getUrl(bucket, key).toString();

        return ReadPreSignedUrlResponse.of(presignedUrl.toString(), objectUrl);
    }
}
