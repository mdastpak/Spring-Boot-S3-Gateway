package br.com.example.davidarchanjo.service.impl;

import br.com.example.davidarchanjo.config.StorageProperties;
import br.com.example.davidarchanjo.enumeration.BucketStrategy;
import br.com.example.davidarchanjo.enumeration.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BucketManagementServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class BucketManagementServiceImplTest {

    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private BucketManagementServiceImpl bucketManagementService;

    @BeforeEach
    void setUp() {
        // Using lenient() to avoid unnecessary stubbing errors
        lenient().when(storageProperties.getBucketSuffix()).thenReturn("storage");
        lenient().when(storageProperties.getSharedBucket()).thenReturn("shared-storage");
    }

    @Test
    void testGetBucketName_SharedWithPrefix_ReturnsSharedBucket() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.SHARED_WITH_PREFIX);

        String bucketName = bucketManagementService.getBucketName("client-001", Environment.DEVELOPMENT);

        assertEquals("shared-storage", bucketName);
    }

    @Test
    void testGetBucketName_PerClient_ReturnsClientBucket() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT);

        String bucketName = bucketManagementService.getBucketName("client-001", Environment.DEVELOPMENT);

        assertEquals("client-001-storage", bucketName);
    }

    @Test
    void testGetBucketName_PerClientPerEnvironment_ReturnsClientEnvBucket() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT_PER_ENVIRONMENT);

        String bucketName = bucketManagementService.getBucketName("client-001", Environment.DEVELOPMENT);

        assertEquals("client-001-dev-storage", bucketName);
    }

    @Test
    void testGetBucketName_PerClientPerEnvironment_Production() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT_PER_ENVIRONMENT);

        String bucketName = bucketManagementService.getBucketName("client-002", Environment.PRODUCTION);

        assertEquals("client-002-prod-storage", bucketName);
    }

    @Test
    void testGetBucketName_SanitizesClientId() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT);

        // Client ID with special characters that should be sanitized
        String bucketName = bucketManagementService.getBucketName("Client_001", Environment.DEVELOPMENT);

        assertEquals("client-001-storage", bucketName);
    }

    @Test
    void testBuildObjectKey_SharedWithPrefix_IncludesClientAndEnvPrefix() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.SHARED_WITH_PREFIX);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-001",
                Environment.DEVELOPMENT,
                "documents",
                "file.pdf"
        );

        assertEquals("client-001/dev/documents/file.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_PerClient_IncludesEnvPrefix() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-001",
                Environment.DEVELOPMENT,
                "documents",
                "file.pdf"
        );

        assertEquals("dev/documents/file.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_PerClientPerEnvironment_NoEnvPrefix() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT_PER_ENVIRONMENT);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-001",
                Environment.DEVELOPMENT,
                "documents",
                "file.pdf"
        );

        // Environment is in bucket name, not in object key
        assertEquals("documents/file.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_NullDirectory_OmitsDirectory() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.SHARED_WITH_PREFIX);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-001",
                Environment.DEVELOPMENT,
                null,
                "file.pdf"
        );

        assertEquals("client-001/dev/file.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_EmptyDirectory_OmitsDirectory() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.SHARED_WITH_PREFIX);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-001",
                Environment.DEVELOPMENT,
                "",
                "file.pdf"
        );

        assertEquals("client-001/dev/file.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_NestedDirectory() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.SHARED_WITH_PREFIX);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-001",
                Environment.PRODUCTION,
                "2025/invoices",
                "invoice-001.pdf"
        );

        assertEquals("client-001/prod/2025/invoices/invoice-001.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_StagingEnvironment() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.SHARED_WITH_PREFIX);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-002",
                Environment.STAGING,
                "reports",
                "report.pdf"
        );

        assertEquals("client-002/staging/reports/report.pdf", objectKey);
    }

    @Test
    void testBuildObjectKey_UATEnvironment() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT);

        String objectKey = bucketManagementService.buildObjectKey(
                "client-003",
                Environment.UAT,
                "test-data",
                "data.csv"
        );

        assertEquals("uat/test-data/data.csv", objectKey);
    }

    @Test
    void testGetBucketName_DifferentClients_UniqueBuckets() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT);

        String bucket1 = bucketManagementService.getBucketName("client-001", Environment.DEVELOPMENT);
        String bucket2 = bucketManagementService.getBucketName("client-002", Environment.DEVELOPMENT);

        assertNotEquals(bucket1, bucket2);
        assertEquals("client-001-storage", bucket1);
        assertEquals("client-002-storage", bucket2);
    }

    @Test
    void testGetBucketName_SameClient_DifferentEnvironments() {
        when(storageProperties.getBucketStrategy()).thenReturn(BucketStrategy.PER_CLIENT_PER_ENVIRONMENT);

        String devBucket = bucketManagementService.getBucketName("client-001", Environment.DEVELOPMENT);
        String prodBucket = bucketManagementService.getBucketName("client-001", Environment.PRODUCTION);

        assertNotEquals(devBucket, prodBucket);
        assertEquals("client-001-dev-storage", devBucket);
        assertEquals("client-001-prod-storage", prodBucket);
    }
}
