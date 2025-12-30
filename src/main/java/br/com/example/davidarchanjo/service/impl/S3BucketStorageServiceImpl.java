package br.com.example.davidarchanjo.service.impl;

import br.com.example.davidarchanjo.exception.BucketNotFoundException;
import br.com.example.davidarchanjo.exception.FileNotFoundException;
import br.com.example.davidarchanjo.exception.FileUploadException;
import br.com.example.davidarchanjo.exception.StorageException;
import br.com.example.davidarchanjo.service.S3BucketStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3BucketStorageServiceImpl implements S3BucketStorageService {

    private final S3Client s3Client;

    @Override
    public void uploadFile(
            String bucketName,
            String keyName,
            Long contentLength,
            String contentType,
            InputStream value
    ) {
        try {
            if (!bucketExists(bucketName)) {
                throw new BucketNotFoundException(bucketName);
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(value, contentLength));
            log.info("File uploaded successfully to bucket '{}': {}", bucketName, keyName);

        } catch (S3Exception e) {
            log.error("Failed to upload file '{}' to bucket '{}': {}", keyName, bucketName, e.getMessage());
            throw new FileUploadException("Failed to upload file: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file '{}' to bucket '{}': {}", keyName, bucketName, e.getMessage());
            throw new FileUploadException("Unexpected error uploading file", e);
        }
    }

    @Override
    public ByteArrayOutputStream downloadFile(
            String bucketName,
            String keyName
    ) {
        try {
            if (!bucketExists(bucketName)) {
                throw new BucketNotFoundException(bucketName);
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                log.info("File downloaded successfully from bucket '{}': {}", bucketName, keyName);
                return outputStream;
            }

        } catch (NoSuchKeyException e) {
            log.error("File '{}' not found in bucket '{}'", keyName, bucketName);
            throw new FileNotFoundException(keyName, bucketName);
        } catch (S3Exception e) {
            log.error("S3 error downloading file '{}' from bucket '{}': {}", keyName, bucketName, e.getMessage());
            throw new StorageException("Failed to download file: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            log.error("IO error downloading file '{}' from bucket '{}': {}", keyName, bucketName, e.getMessage());
            throw new StorageException("IO error downloading file", e);
        }
    }

    @Override
    public List<String> listFiles(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                throw new BucketNotFoundException(bucketName);
            }

            List<String> keys = new ArrayList<>();
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.listObjectsV2Paginator(listObjectsRequest)
                    .contents()
                    .stream()
                    .filter(item -> !item.key().endsWith("/"))
                    .map(S3Object::key)
                    .forEach(keys::add);

            log.info("Listed {} files in bucket '{}'", keys.size(), bucketName);
            return keys;

        } catch (S3Exception e) {
            log.error("Failed to list files in bucket '{}': {}", bucketName, e.getMessage());
            throw new StorageException("Failed to list files: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public void deleteFile(
            String bucketName,
            String keyName
    ) {
        try {
            if (!bucketExists(bucketName)) {
                throw new BucketNotFoundException(bucketName);
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from bucket '{}': {}", bucketName, keyName);

        } catch (S3Exception e) {
            log.error("Failed to delete file '{}' from bucket '{}': {}", keyName, bucketName, e.getMessage());
            throw new StorageException("Failed to delete file: " + e.awsErrorDetails().errorMessage(), e);
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
}

