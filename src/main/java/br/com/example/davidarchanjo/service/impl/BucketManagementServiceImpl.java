package br.com.example.davidarchanjo.service.impl;

import br.com.example.davidarchanjo.config.StorageProperties;
import br.com.example.davidarchanjo.enumeration.BucketStrategy;
import br.com.example.davidarchanjo.enumeration.Environment;
import br.com.example.davidarchanjo.exception.StorageException;
import br.com.example.davidarchanjo.service.BucketManagementService;
import br.com.example.davidarchanjo.util.PathSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class BucketManagementServiceImpl implements BucketManagementService {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public String getBucketName(String clientId, Environment environment) {
        BucketStrategy strategy = storageProperties.getBucketStrategy();

        switch (strategy) {
            case SHARED_WITH_PREFIX:
                return storageProperties.getSharedBucket();

            case PER_CLIENT:
                return String.format("%s-%s", sanitizeForBucketName(clientId), storageProperties.getBucketSuffix());

            case PER_CLIENT_PER_ENVIRONMENT:
                return String.format("%s-%s-%s",
                        sanitizeForBucketName(clientId),
                        environment.getValue(),
                        storageProperties.getBucketSuffix());

            default:
                throw new StorageException("Invalid bucket strategy: " + strategy);
        }
    }

    @Override
    public String buildObjectKey(String clientId, Environment environment, String directory, String fileName) {
        BucketStrategy strategy = storageProperties.getBucketStrategy();

        StringBuilder keyBuilder = new StringBuilder();

        // Add client prefix for SHARED_WITH_PREFIX strategy
        if (strategy == BucketStrategy.SHARED_WITH_PREFIX) {
            keyBuilder.append(sanitizeForPath(clientId)).append("/");
        }

        // Add environment prefix (except for PER_CLIENT_PER_ENVIRONMENT where it's in bucket name)
        if (strategy != BucketStrategy.PER_CLIENT_PER_ENVIRONMENT) {
            keyBuilder.append(environment.getValue()).append("/");
        }

        // Add directory if provided
        if (directory != null && !directory.trim().isEmpty()) {
            String sanitizedDir = PathSanitizer.sanitizeDirectory(directory);
            if (!sanitizedDir.isEmpty()) {
                keyBuilder.append(sanitizedDir).append("/");
            }
        }

        // Add file name
        String sanitizedFileName = PathSanitizer.sanitizeFileName(fileName);
        keyBuilder.append(sanitizedFileName);

        return keyBuilder.toString();
    }

    @Override
    public void createBucketIfNotExists(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                log.info("Creating bucket: {}", bucketName);

                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();

                s3Client.createBucket(createBucketRequest);
                log.info("Bucket created successfully: {}", bucketName);

                // Wait for bucket to be available
                waitForBucket(bucketName);
            }
        } catch (S3Exception e) {
            log.error("Failed to create bucket '{}': {}", bucketName, e.getMessage());
            throw new StorageException("Failed to create bucket: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            return true;

        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            log.warn("Error checking if bucket '{}' exists: {}", bucketName, e.getMessage());
            return false;
        }
    }

    /**
     * Wait for bucket to become available
     */
    private void waitForBucket(String bucketName) {
        try {
            log.debug("Waiting for bucket '{}' to be available", bucketName);
            s3Client.waiter().waitUntilBucketExists(
                    HeadBucketRequest.builder().bucket(bucketName).build()
            );
            log.debug("Bucket '{}' is now available", bucketName);
        } catch (Exception e) {
            log.warn("Error waiting for bucket: {}", e.getMessage());
        }
    }

    /**
     * Sanitize string for use in bucket name
     * Bucket names must be lowercase, alphanumeric, and hyphens only
     */
    private String sanitizeForBucketName(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Sanitize string for use in object path
     */
    private String sanitizeForPath(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
