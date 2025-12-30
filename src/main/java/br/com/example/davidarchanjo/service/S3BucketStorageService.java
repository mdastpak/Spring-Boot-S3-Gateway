package br.com.example.davidarchanjo.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Service for object storage operations (S3/MinIO)
 */
public interface S3BucketStorageService {

    /**
     * Upload file to object storage
     *
     * @param bucketName    Bucket name
     * @param keyName       File key/name
     * @param contentLength File size in bytes
     * @param contentType   MIME type
     * @param value         File input stream
     */
    void uploadFile(
            String bucketName,
            String keyName,
            Long contentLength,
            String contentType,
            InputStream value
    );

    /**
     * Download file from object storage
     *
     * @param bucketName Bucket name
     * @param keyName    File key/name
     * @return File content as byte array stream
     */
    ByteArrayOutputStream downloadFile(
            String bucketName,
            String keyName
    );

    /**
     * List all files in a bucket
     *
     * @param bucketName Bucket name
     * @return List of file keys
     */
    List<String> listFiles(String bucketName);

    /**
     * Delete file from object storage
     *
     * @param bucketName Bucket name
     * @param keyName    File key/name
     */
    void deleteFile(
            String bucketName,
            String keyName
    );

    /**
     * Check if bucket exists
     *
     * @param bucketName Bucket name
     * @return true if bucket exists
     */
    boolean bucketExists(String bucketName);
}

