package br.com.example.davidarchanjo.dto;

import br.com.example.davidarchanjo.enumeration.Environment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO showing client-bucket-stage mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Client bucket mapping per environment")
public class ClientBucketMappingDTO {

    @Schema(description = "Client identifier", example = "client-001")
    private String clientId;

    @Schema(description = "Client name", example = "Acme Corporation")
    private String clientName;

    @Schema(description = "Bucket mappings per environment")
    private Map<Environment, String> bucketMappings;

    @Schema(description = "Bucket strategy used", example = "SHARED_WITH_PREFIX")
    private String bucketStrategy;

    @Schema(description = "Storage usage in bytes", example = "1048576")
    private Long storageUsageBytes;

    @Schema(description = "Storage quota in MB", example = "1000")
    private Long quotaMb;

    @Schema(description = "Quota usage percentage", example = "10.5")
    private Double quotaUsagePercent;
}
