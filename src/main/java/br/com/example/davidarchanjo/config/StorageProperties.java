package br.com.example.davidarchanjo.config;

import br.com.example.davidarchanjo.enumeration.BucketStrategy;
import br.com.example.davidarchanjo.enumeration.DuplicateFileStrategy;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Storage provider type: s3 or minio
     */
    @NotBlank
    private String provider = "s3";

    /**
     * AWS Region (for S3) or MinIO region
     */
    @NotBlank
    private String region = "us-east-1";

    /**
     * Custom endpoint URL (required for MinIO, optional for S3)
     */
    private String endpointUrl;

    /**
     * Access key ID
     */
    private String accessKey;

    /**
     * Secret access key
     */
    private String secretKey;

    /**
     * Default bucket name (legacy, kept for backward compatibility)
     */
    private String defaultBucket;

    /**
     * Enable path-style access (required for MinIO)
     */
    private boolean pathStyleAccess = false;

    /**
     * Maximum file size in MB
     */
    private long maxFileSizeMb = 10;

    // ===== Multi-Tenancy Configuration =====

    /**
     * Bucket organization strategy
     */
    private BucketStrategy bucketStrategy = BucketStrategy.SHARED_WITH_PREFIX;

    /**
     * Shared bucket name (for SHARED_WITH_PREFIX strategy)
     */
    private String sharedBucket = "shared-storage";

    /**
     * Bucket suffix (for PER_CLIENT and PER_CLIENT_PER_ENVIRONMENT strategies)
     */
    private String bucketSuffix = "storage";

    /**
     * Auto-create buckets if they don't exist
     */
    private boolean autoCreateBuckets = true;

    /**
     * Duplicate file handling strategy
     */
    private DuplicateFileStrategy duplicateFileStrategy = DuplicateFileStrategy.UUID_SUFFIX;

    /**
     * Enable multi-tenancy support (bucket prefixing/separation)
     */
    private boolean multiTenancyEnabled = true;

    /**
     * Enable storage quota enforcement
     */
    private boolean enforceQuota = true;
}

