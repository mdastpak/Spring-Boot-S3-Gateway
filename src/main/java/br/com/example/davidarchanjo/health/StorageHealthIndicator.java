package br.com.example.davidarchanjo.health;

import br.com.example.davidarchanjo.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageHealthIndicator implements HealthIndicator {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public Health health() {
        try {
            // Test connection by listing buckets
            s3Client.listBuckets(ListBucketsRequest.builder().build());

            return Health.up()
                    .withDetail("provider", storageProperties.getProvider())
                    .withDetail("region", storageProperties.getRegion())
                    .withDetail("endpoint", storageProperties.getEndpointUrl() != null ?
                            storageProperties.getEndpointUrl() : "default")
                    .withDetail("status", "Connection successful")
                    .build();

        } catch (Exception e) {
            log.error("Storage health check failed: {}", e.getMessage());

            return Health.down()
                    .withDetail("provider", storageProperties.getProvider())
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Connection failed")
                    .build();
        }
    }
}
