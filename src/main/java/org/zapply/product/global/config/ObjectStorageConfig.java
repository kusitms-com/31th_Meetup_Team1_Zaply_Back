package org.zapply.product.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class ObjectStorageConfig {

    @Value("${cloud.ncp.object-storage.access-key}")
    private String accessKey;
    @Value("${cloud.ncp.object-storage.secret-key}")
    private String secretKey;
    @Value("${cloud.ncp.object-storage.region-name}")
    private String region;
    @Value("${cloud.ncp.object-storage.end-point}")
    private String endpoint;

    @Bean
    public AmazonS3 ncpS3Client() {
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()

                // NCP 전용 endpoint, region
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region)
                )

                // 버킷/객체 경로 방식으로 URL 생성
                // virtual-host 방식 대신 path-style 사용
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }
}
