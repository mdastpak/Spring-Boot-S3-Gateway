package br.com.example.davidarchanjo.exception;

public class BucketNotFoundException extends StorageException {

    public BucketNotFoundException(String bucketName) {
        super(String.format("Bucket '%s' not found", bucketName));
    }
}
