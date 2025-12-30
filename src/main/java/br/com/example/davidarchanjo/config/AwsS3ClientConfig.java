package br.com.example.davidarchanjo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StorageProperties.class)
public class AwsS3ClientConfig {

    private final StorageProperties storageProperties;

    @Bean
    public S3Client s3Client() {
        log.info("Configuring S3 client for provider: {}", storageProperties.getProvider());

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(storageProperties.getRegion()))
                .credentialsProvider(getCredentialsProvider());

        // Configure for MinIO or custom endpoint
        if (storageProperties.getEndpointUrl() != null && !storageProperties.getEndpointUrl().isEmpty()) {
            log.info("Using custom endpoint: {}", storageProperties.getEndpointUrl());
            builder.endpointOverride(URI.create(storageProperties.getEndpointUrl()));
        }

        // Enable path-style access for MinIO
        if (storageProperties.isPathStyleAccess()) {
            log.info("Enabling path-style access");
            builder.forcePathStyle(true);
        }

        return builder.build();
    }

    private AwsCredentialsProvider getCredentialsProvider() {
        // Use static credentials if provided, otherwise use default credentials chain
        if (storageProperties.getAccessKey() != null && storageProperties.getSecretKey() != null) {
            String accessKey = storageProperties.getAccessKey();
            String maskedKey = accessKey.substring(0, Math.min(4, accessKey.length())) + "...";
            log.info("Using static credentials with access key: {}", maskedKey);
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            storageProperties.getAccessKey(),
                            storageProperties.getSecretKey()
                    )
            );
        }

        log.info("Using default credentials provider chain");
        return DefaultCredentialsProvider.builder().build();
    }
}

