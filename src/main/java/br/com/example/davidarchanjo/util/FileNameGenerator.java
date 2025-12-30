package br.com.example.davidarchanjo.util;

import br.com.example.davidarchanjo.enumeration.DuplicateFileStrategy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for generating unique file names
 */
@Slf4j
public final class FileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private FileNameGenerator() {
        // Utility class
    }

    /**
     * Generate a unique file name based on strategy
     *
     * @param originalFileName Original file name
     * @param strategy         Duplicate file strategy
     * @param version          Version number (for VERSION strategy)
     * @return Generated file name
     */
    public static String generateFileName(String originalFileName, DuplicateFileStrategy strategy, Integer version) {
        String baseName = getBaseName(originalFileName);
        String extension = getExtension(originalFileName);

        switch (strategy) {
            case UUID_SUFFIX:
                return generateWithUUID(baseName, extension);

            case TIMESTAMP_SUFFIX:
                return generateWithTimestamp(baseName, extension);

            case VERSION:
                return generateWithVersion(baseName, extension, version);

            case OVERWRITE:
            case REJECT:
            default:
                return originalFileName;
        }
    }

    /**
     * Generate file name with UUID suffix
     */
    private static String generateWithUUID(String baseName, String extension) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s%s", baseName, uuid, extension);
    }

    /**
     * Generate file name with timestamp suffix
     */
    private static String generateWithTimestamp(String baseName, String extension) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return String.format("%s_%s%s", baseName, timestamp, extension);
    }

    /**
     * Generate file name with version suffix
     */
    private static String generateWithVersion(String baseName, String extension, Integer version) {
        if (version == null || version <= 1) {
            return baseName + extension;
        }
        return String.format("%s_v%d%s", baseName, version, extension);
    }

    /**
     * Extract base name (without extension)
     */
    private static String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * Extract file extension (including dot)
     */
    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

    /**
     * Generate a unique ID
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a short unique ID (8 characters)
     */
    public static String generateShortUniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
