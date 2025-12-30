package br.com.example.davidarchanjo.exception;

public class FileNotFoundException extends StorageException {

    public FileNotFoundException(String fileName, String bucketName) {
        super(String.format("File '%s' not found in bucket '%s'", fileName, bucketName));
    }
}
