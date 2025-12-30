package br.com.example.davidarchanjo.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Strategy for handling duplicate file names
 */
@Getter
@RequiredArgsConstructor
public enum DuplicateFileStrategy {
    /**
     * Overwrite existing file
     */
    OVERWRITE("overwrite", "Replace existing file"),

    /**
     * Add UUID to filename
     * Example: document.pdf -> document_a1b2c3d4.pdf
     */
    UUID_SUFFIX("uuid", "Append UUID to filename"),

    /**
     * Add timestamp to filename
     * Example: document.pdf -> document_20251230_103045.pdf
     */
    TIMESTAMP_SUFFIX("timestamp", "Append timestamp to filename"),

    /**
     * Keep versioned copies
     * Example: document.pdf, document_v2.pdf, document_v3.pdf
     */
    VERSION("version", "Create versioned copies"),

    /**
     * Reject duplicate upload
     */
    REJECT("reject", "Reject upload if file exists");

    private final String code;
    private final String description;

    public static DuplicateFileStrategy fromCode(String code) {
        for (DuplicateFileStrategy strategy : values()) {
            if (strategy.code.equalsIgnoreCase(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Invalid duplicate file strategy: " + code);
    }
}
