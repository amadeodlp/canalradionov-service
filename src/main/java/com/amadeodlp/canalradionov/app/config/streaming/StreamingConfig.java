package com.amadeodlp.canalradionov.app.config.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableWebSocket
public class StreamingConfig implements WebSocketConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(StreamingConfig.class);
    
    @Value("${AWS_REGION:us-east-1}")
    private String awsRegion;
    
    @Value("${AWS_ACCESS_KEY_ID:default-key}")
    private String accessKey;
    
    @Value("${AWS_SECRET_ACCESS_KEY:default-secret}")
    private String secretKey;
    
    @Value("${AWS_SESSION_TOKEN:}")
    private String sessionToken;
    
    @Value("${app.media.bucket-name:canal-radio-nov-media}")
    private String mediaBucketName;
    
    @Value("${app.media.cloudfront-domain:}")
    private String cloudfrontDomain;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(streamingWebSocketHandler(), "/ws/streaming")
                .setAllowedOrigins("*");
    }
    
    @Bean
    public WebSocketHandler streamingWebSocketHandler() {
        return new StreamingWebSocketHandler(s3Client(), cloudfrontDomain, mediaBucketName);
    }
    
    @Bean
    public S3Client s3Client() {
        if (accessKey.equals("default-key") || secretKey.equals("default-secret")) {
            LOG.warn("Using default credentials for S3Client. This should be changed in production.");
        }
        
        AwsSessionCredentials credentials = AwsSessionCredentials.create(
                accessKey,
                secretKey,
                sessionToken
        );
        
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
    
    @Bean
    public CloudFrontClient cloudFrontClient() {
        if (accessKey.equals("default-key") || secretKey.equals("default-secret")) {
            LOG.warn("Using default credentials for CloudFrontClient. This should be changed in production.");
        }
        
        AwsSessionCredentials credentials = AwsSessionCredentials.create(
                accessKey,
                secretKey,
                sessionToken
        );
        
        return CloudFrontClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
