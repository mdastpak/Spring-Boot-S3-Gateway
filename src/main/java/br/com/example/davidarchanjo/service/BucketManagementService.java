package br.com.example.davidarchanjo.service;

import br.com.example.davidarchanjo.enumeration.Environment;

/**
 * Service for managing bucket names and paths based on strategy
 */
public interface BucketManagementService {

    /**
     * Get bucket name for a client and environment
     *
     * @param clientId    Client identifier
     * @param environment Environment/stage
     * @return Bucket name
     */
    String getBucketName(String clientId, Environment environment);

    /**
     * Build object key/path with client and environment prefixes
     *
     * @param clientId    Client identifier
     * @param environment Environment/stage
     * @param directory   Optional directory path
     * @param fileName    File name
     * @return Complete object key
     */
    String buildObjectKey(String clientId, Environment environment, String directory, String fileName);

    /**
     * Create bucket if it doesn't exist
     *
     * @param bucketName Bucket name
     */
    void createBucketIfNotExists(String bucketName);

    /**
     * Check if bucket exists
     *
     * @param bucketName Bucket name
     * @return true if exists
     */
    boolean bucketExists(String bucketName);
}
