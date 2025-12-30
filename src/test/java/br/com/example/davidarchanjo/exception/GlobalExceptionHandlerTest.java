package br.com.example.davidarchanjo.exception;

import br.com.example.davidarchanjo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private static final String TEST_URI = "/api/v1/storage/test-bucket/upload";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(TEST_URI);
    }

    @Test
    void testHandleFileNotFoundException() {
        String fileName = "test.pdf";
        String bucketName = "test-bucket";
        FileNotFoundException exception = new FileNotFoundException(fileName, bucketName);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileNotFoundException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains(fileName));
        assertTrue(response.getBody().getMessage().contains(bucketName));
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleBucketNotFoundException() {
        String bucketName = "test-bucket";
        BucketNotFoundException exception = new BucketNotFoundException(bucketName);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBucketNotFoundException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains(bucketName));
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleFileUploadException() {
        String errorMessage = "Failed to upload file";
        FileUploadException exception = new FileUploadException(errorMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileUploadException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleStorageException() {
        String errorMessage = "Storage operation failed";
        StorageException exception = new StorageException(errorMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStorageException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(10_000_000L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxUploadSizeExceededException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(413, response.getBody().getStatus());
        assertEquals("File size exceeds maximum allowed limit", response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleValidationException_SingleError() {
        FieldError fieldError = new FieldError("authRequest", "clientId", "Client ID is required");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        assertTrue(response.getBody().getMessage().contains("Client ID is required"));
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleValidationException_MultipleErrors() {
        FieldError error1 = new FieldError("authRequest", "clientId", "Client ID is required");
        FieldError error2 = new FieldError("authRequest", "apiKey", "API Key is required");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        assertTrue(response.getBody().getMessage().contains("Client ID is required"));
        assertTrue(response.getBody().getMessage().contains("API Key is required"));
        assertEquals(TEST_URI, response.getBody().getPath());
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new RuntimeException("Unexpected error occurred");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals(TEST_URI, response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleGenericException_NullPointerException() {
        Exception exception = new NullPointerException("Null pointer error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void testHandleGenericException_IllegalArgumentException() {
        Exception exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void testErrorResponse_ContainsTimestamp() {
        StorageException exception = new StorageException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStorageException(exception, request);

        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testErrorResponse_ContainsCorrectStatusCode() {
        FileNotFoundException notFound = new FileNotFoundException("file.pdf", "bucket");
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleFileNotFoundException(notFound, request);
        assertEquals(404, notFoundResponse.getBody().getStatus());

        StorageException serverError = new StorageException("Internal error");
        ResponseEntity<ErrorResponse> serverErrorResponse = exceptionHandler.handleStorageException(serverError, request);
        assertEquals(500, serverErrorResponse.getBody().getStatus());

        MaxUploadSizeExceededException tooLarge = new MaxUploadSizeExceededException(1000L);
        ResponseEntity<ErrorResponse> tooLargeResponse = exceptionHandler.handleMaxUploadSizeExceededException(tooLarge, request);
        assertEquals(413, tooLargeResponse.getBody().getStatus());
    }

    @Test
    void testFileNotFoundException_WithDifferentPaths() {
        when(request.getRequestURI()).thenReturn("/api/v1/storage/custom-bucket/download/file.pdf");

        FileNotFoundException exception = new FileNotFoundException("file.pdf", "custom-bucket");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileNotFoundException(exception, request);

        assertEquals("/api/v1/storage/custom-bucket/download/file.pdf", response.getBody().getPath());
    }

    @Test
    void testHandleStorageException_WithCause() {
        Throwable cause = new RuntimeException("Root cause error");
        StorageException exception = new StorageException("Storage failed", cause);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleStorageException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Storage failed", response.getBody().getMessage());
    }

    @Test
    void testHandleFileUploadException_WithCause() {
        Throwable cause = new RuntimeException("Network error");
        FileUploadException exception = new FileUploadException("Upload failed", cause);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileUploadException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Upload failed", response.getBody().getMessage());
    }
}
