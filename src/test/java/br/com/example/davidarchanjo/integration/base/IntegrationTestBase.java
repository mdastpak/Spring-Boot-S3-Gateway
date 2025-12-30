package br.com.example.davidarchanjo.integration.base;

import br.com.example.davidarchanjo.application.SpringBootAwsS3Application;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with MinIO Testcontainer
 *
 * This class:
 * - Starts a real MinIO instance using Testcontainers
 * - Configures Spring Boot to connect to the test MinIO
 * - Sets up REST Assured for API testing
 * - Provides utility methods for all integration tests
 */
@SpringBootTest(
        classes = SpringBootAwsS3Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @LocalServerPort
    protected int port;

    /**
     * MinIO Testcontainer - runs real MinIO in Docker
     * Using official MinIO image
     */
    @Container
    protected static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:RELEASE.2024-12-18T13-15-44Z")
            .withUserName("minioadmin")
            .withPassword("minioadmin");

    /**
     * Dynamically configure Spring properties to use the test MinIO container
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure MinIO connection
        registry.add("storage.endpoint-url", minioContainer::getS3URL);
        registry.add("storage.access-key", minioContainer::getUserName);
        registry.add("storage.secret-key", minioContainer::getPassword);
        registry.add("storage.region", () -> "us-east-1");
        registry.add("storage.path-style-access", () -> true);
        registry.add("storage.provider", () -> "minio");

        // Test bucket configuration
        registry.add("storage.shared-bucket", () -> "test-shared-storage");
        registry.add("storage.auto-create-buckets", () -> true);
        registry.add("storage.bucket-strategy", () -> "SHARED_WITH_PREFIX");
    }

    /**
     * Setup REST Assured before each test
     */
    @BeforeEach
    public void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * Get the MinIO container URL
     */
    protected String getMinioUrl() {
        return minioContainer.getS3URL();
    }

    /**
     * Get the MinIO access key
     */
    protected String getMinioAccessKey() {
        return minioContainer.getUserName();
    }

    /**
     * Get the MinIO secret key
     */
    protected String getMinioSecretKey() {
        return minioContainer.getPassword();
    }

    /**
     * Check if MinIO container is running
     */
    protected boolean isMinioRunning() {
        return minioContainer.isRunning();
    }
}
