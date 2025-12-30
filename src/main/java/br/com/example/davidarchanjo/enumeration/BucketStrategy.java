package br.com.example.davidarchanjo.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Bucket strategy for multi-tenant storage organization
 */
@Getter
@RequiredArgsConstructor
public enum BucketStrategy {
    /**
     * Single shared bucket with client prefixes
     * Example: shared-bucket/client-1/dev/files/
     */
    SHARED_WITH_PREFIX("shared-prefix", "Single bucket with client and environment prefixes"),

    /**
     * Separate bucket per client
     * Example: client-1-bucket/dev/files/
     */
    PER_CLIENT("per-client", "Dedicated bucket per client"),

    /**
     * Separate bucket per client and environment
     * Example: client-1-dev-bucket/files/
     */
    PER_CLIENT_PER_ENVIRONMENT("per-client-env", "Dedicated bucket per client and environment");

    private final String code;
    private final String description;

    public static BucketStrategy fromCode(String code) {
        for (BucketStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Invalid bucket strategy: " + code);
    }
}
