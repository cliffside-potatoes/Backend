package com.potatoes.Naengu.file.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class S3Config {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    // AWS 자격증명(AWS Access Key와 Secret Key)을 제공하는 Bean
    @Bean
    @Primary  // 여러 개의 자격증명 Bean이 있을 때 기본값으로 이 Bean을 사용하도록 지정
    public BasicAWSCredentials awsCredentialsProvider(){
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    // AmazonS3 클라이언트를 생성하여 S3와 상호작용할 수 있게 하는 Bean
    @Bean
    public AmazonS3 amazonS3() {
        // BasicAWSCredentials 객체 생성 (Access Key와 Secret Key 사용)
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        // AmazonS3 클라이언트를 설정하고 반환
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))  // S3 클라이언트의 엔드포인트와 리전 설정
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))  // 위에서 생성한 자격증명 제공
                .build();  // 클라이언트 빌드 후 반환
    }

}
