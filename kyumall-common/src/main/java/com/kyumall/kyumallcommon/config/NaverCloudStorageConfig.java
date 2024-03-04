package com.kyumall.kyumallcommon.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverCloudStorageConfig {
  @Value("${cloud.naver.credentials.access-key}")
  private String accessKey;
  @Value("${cloud.naver.credentials.secret-key}")
  private String accessSecret;
  @Value("${cloud.naver.region.static}")
  private String region;
  @Value("${cloud.naver.object-storage.endpoint}")
  private String endPoint;

  @Bean
  public AmazonS3 s3Client() {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
    return AmazonS3ClientBuilder.standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
        .build();
  }
}
