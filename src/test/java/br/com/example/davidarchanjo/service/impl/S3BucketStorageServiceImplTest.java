package br.com.example.davidarchanjo.service.impl;

import br.com.example.davidarchanjo.exception.BucketNotFoundException;
import br.com.example.davidarchanjo.exception.FileNotFoundException;
import br.com.example.davidarchanjo.exception.FileUploadException;
import br.com.example.davidarchanjo.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for S3BucketStorageServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class S3BucketStorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3BucketStorageServiceImpl service;

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_KEY = "test-file.pdf";
    private static final String TEST_CONTENT_TYPE = "application/pdf";
    private static final Long TEST_CONTENT_LENGTH = 1024L;

    @BeforeEach
    void setUp() {
        // Mock bucket exists by default using lenient to avoid unnecessary stubbing warnings
        lenient().when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenReturn(HeadBucketResponse.builder().build());
    }

    @Test
    void testUploadFile_Success() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        assertDoesNotThrow(() -> service.uploadFile(
                TEST_BUCKET,
                TEST_KEY,
                TEST_CONTENT_LENGTH,
                TEST_CONTENT_TYPE,
                inputStream
        ));

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_BucketNotFound_ThrowsException() {
        // Reset the default stubbing for this test
        reset(s3Client);
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().build());

        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        // The service wraps BucketNotFoundException in FileUploadException
        assertThrows(FileUploadException.class, () -> service.uploadFile(
                TEST_BUCKET,
                TEST_KEY,
                TEST_CONTENT_LENGTH,
                TEST_CONTENT_TYPE,
                inputStream
        ));

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_S3Exception_ThrowsFileUploadException() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder()
                        .message("Upload failed")
                        .awsErrorDetails(AwsErrorDetails.builder()
                                .errorMessage("Access denied")
                                .build())
                        .build());

        assertThrows(FileUploadException.class, () -> service.uploadFile(
                TEST_BUCKET,
                TEST_KEY,
                TEST_CONTENT_LENGTH,
                TEST_CONTENT_TYPE,
                inputStream
        ));
    }

    @Test
    void testDownloadFile_Success() {
        byte[] testContent = "test file content".getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(testContent);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(GetObjectResponse.builder().build(), byteArrayInputStream);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(responseInputStream);

        ByteArrayOutputStream result = service.downloadFile(TEST_BUCKET, TEST_KEY);

        assertNotNull(result);
        assertArrayEquals(testContent, result.toByteArray());
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testDownloadFile_BucketNotFound_ThrowsException() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().build());

        assertThrows(BucketNotFoundException.class, () ->
                service.downloadFile(TEST_BUCKET, TEST_KEY)
        );
    }

    @Test
    void testDownloadFile_FileNotFound_ThrowsException() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        assertThrows(FileNotFoundException.class, () ->
                service.downloadFile(TEST_BUCKET, TEST_KEY)
        );
    }

    @Test
    void testDownloadFile_S3Exception_ThrowsStorageException() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("Download failed")
                        .awsErrorDetails(AwsErrorDetails.builder()
                                .errorMessage("Access denied")
                                .build())
                        .build());

        assertThrows(StorageException.class, () ->
                service.downloadFile(TEST_BUCKET, TEST_KEY)
        );
    }

    @Test
    void testListFiles_Success() {
        S3Object object1 = S3Object.builder().key("file1.pdf").build();
        S3Object object2 = S3Object.builder().key("file2.txt").build();
        S3Object directory = S3Object.builder().key("folder/").build();

        ListObjectsV2Iterable paginator = mock(ListObjectsV2Iterable.class);
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenReturn(paginator);

        when(paginator.contents())
                .thenReturn(() -> List.of(object1, object2, directory).iterator());

        List<String> files = service.listFiles(TEST_BUCKET);

        assertNotNull(files);
        assertEquals(2, files.size());
        assertTrue(files.contains("file1.pdf"));
        assertTrue(files.contains("file2.txt"));
        assertFalse(files.contains("folder/")); // Directories should be filtered out
    }

    @Test
    void testListFiles_EmptyBucket_ReturnsEmptyList() {
        ListObjectsV2Iterable paginator = mock(ListObjectsV2Iterable.class);
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenReturn(paginator);

        when(paginator.contents())
                .thenReturn(() -> new ArrayList<S3Object>().iterator());

        List<String> files = service.listFiles(TEST_BUCKET);

        assertNotNull(files);
        assertTrue(files.isEmpty());
    }

    @Test
    void testListFiles_BucketNotFound_ThrowsException() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().build());

        assertThrows(BucketNotFoundException.class, () ->
                service.listFiles(TEST_BUCKET)
        );
    }

    @Test
    void testListFiles_S3Exception_ThrowsStorageException() {
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder()
                        .message("List failed")
                        .awsErrorDetails(AwsErrorDetails.builder()
                                .errorMessage("Access denied")
                                .build())
                        .build());

        assertThrows(StorageException.class, () ->
                service.listFiles(TEST_BUCKET)
        );
    }

    @Test
    void testDeleteFile_Success() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        assertDoesNotThrow(() -> service.deleteFile(TEST_BUCKET, TEST_KEY));

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteFile_BucketNotFound_ThrowsException() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().build());

        assertThrows(BucketNotFoundException.class, () ->
                service.deleteFile(TEST_BUCKET, TEST_KEY)
        );

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteFile_S3Exception_ThrowsStorageException() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("Delete failed")
                        .awsErrorDetails(AwsErrorDetails.builder()
                                .errorMessage("Access denied")
                                .build())
                        .build());

        assertThrows(StorageException.class, () ->
                service.deleteFile(TEST_BUCKET, TEST_KEY)
        );
    }

    @Test
    void testBucketExists_BucketExists_ReturnsTrue() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenReturn(HeadBucketResponse.builder().build());

        boolean exists = service.bucketExists(TEST_BUCKET);

        assertTrue(exists);
    }

    @Test
    void testBucketExists_BucketNotFound_ReturnsFalse() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(NoSuchBucketException.builder().build());

        boolean exists = service.bucketExists(TEST_BUCKET);

        assertFalse(exists);
    }

    @Test
    void testBucketExists_S3Exception_ReturnsFalse() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("Check failed")
                        .build());

        boolean exists = service.bucketExists(TEST_BUCKET);

        assertFalse(exists);
    }

    @Test
    void testUploadFile_VerifiesRequestParameters() {
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        service.uploadFile(TEST_BUCKET, TEST_KEY, TEST_CONTENT_LENGTH, TEST_CONTENT_TYPE, inputStream);

        verify(s3Client).putObject(argThat((PutObjectRequest request) ->
                request.bucket().equals(TEST_BUCKET) &&
                        request.key().equals(TEST_KEY) &&
                        request.contentType().equals(TEST_CONTENT_TYPE) &&
                        request.contentLength().equals(TEST_CONTENT_LENGTH)
        ), any(RequestBody.class));
    }

    @Test
    void testDownloadFile_VerifiesRequestParameters() {
        byte[] testContent = "test content".getBytes();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(testContent);

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(GetObjectResponse.builder().build(), byteArrayInputStream);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(responseInputStream);

        service.downloadFile(TEST_BUCKET, TEST_KEY);

        verify(s3Client).getObject(argThat((GetObjectRequest request) ->
                request.bucket().equals(TEST_BUCKET) &&
                        request.key().equals(TEST_KEY)
        ));
    }

    @Test
    void testDeleteFile_VerifiesRequestParameters() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        service.deleteFile(TEST_BUCKET, TEST_KEY);

        verify(s3Client).deleteObject(argThat((DeleteObjectRequest request) ->
                request.bucket().equals(TEST_BUCKET) &&
                        request.key().equals(TEST_KEY)
        ));
    }
}
