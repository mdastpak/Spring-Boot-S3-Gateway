package br.com.example.davidarchanjo.util;

import br.com.example.davidarchanjo.exception.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Utility class for sanitizing and validating file paths
 * Prevents path traversal attacks and ensures safe file paths
 */
@Slf4j
public final class PathSanitizer {

    // Patterns to detect path traversal attempts
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(".*[/\\\\]\\.\\.([/\\\\].*)?");
    private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile("^([a-zA-Z]:)?[/\\\\].*");
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[<>:\"|?*\\x00-\\x1F]");

    // Reserved names (Windows)
    private static final String[] RESERVED_NAMES = {
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    };

    private PathSanitizer() {
        // Utility class
    }

    /**
     * Sanitize a file path to prevent security vulnerabilities
     *
     * @param path Original path
     * @return Sanitized path
     * @throws StorageException if path is invalid or malicious
     */
    public static String sanitizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new StorageException("Path cannot be null or empty");
        }

        String sanitized = path.trim();

        // Check for path traversal attempts
        if (PATH_TRAVERSAL_PATTERN.matcher(sanitized).matches()) {
            log.error("Path traversal attempt detected: {}", path);
            throw new StorageException("Invalid path: path traversal detected");
        }

        // Check for absolute paths
        if (ABSOLUTE_PATH_PATTERN.matcher(sanitized).matches()) {
            log.error("Absolute path not allowed: {}", path);
            throw new StorageException("Invalid path: absolute paths not allowed");
        }

        // Check for invalid characters
        if (INVALID_CHARS_PATTERN.matcher(sanitized).find()) {
            log.error("Invalid characters in path: {}", path);
            throw new StorageException("Invalid path: contains illegal characters");
        }

        // Normalize the path
        try {
            sanitized = Paths.get(sanitized).normalize().toString();
        } catch (InvalidPathException e) {
            log.error("Invalid path format: {}", path, e);
            throw new StorageException("Invalid path format: " + e.getMessage());
        }

        // Check if path tries to escape (after normalization)
        if (sanitized.startsWith("..") || sanitized.contains("/../") || sanitized.contains("\\..\\")) {
            log.error("Path escape attempt after normalization: {}", sanitized);
            throw new StorageException("Invalid path: escapes base directory");
        }

        // Replace backslashes with forward slashes for consistency
        sanitized = sanitized.replace('\\', '/');

        // Remove leading slash
        if (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1);
        }

        // Check for reserved names (Windows)
        String fileName = getFileName(sanitized);
        if (isReservedName(fileName)) {
            log.error("Reserved file name: {}", fileName);
            throw new StorageException("Invalid path: uses reserved name");
        }

        return sanitized;
    }

    /**
     * Sanitize a directory path
     *
     * @param directory Original directory
     * @return Sanitized directory
     */
    public static String sanitizeDirectory(String directory) {
        if (directory == null || directory.trim().isEmpty()) {
            return "";
        }

        String sanitized = sanitizePath(directory);

        // Ensure directory doesn't end with slash
        if (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }

        return sanitized;
    }

    /**
     * Sanitize a file name (without directory)
     *
     * @param fileName Original file name
     * @return Sanitized file name
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new StorageException("File name cannot be null or empty");
        }

        String sanitized = fileName.trim();

        // File name should not contain path separators
        if (sanitized.contains("/") || sanitized.contains("\\")) {
            log.error("File name contains path separators: {}", fileName);
            throw new StorageException("Invalid file name: cannot contain path separators");
        }

        // Check for invalid characters
        if (INVALID_CHARS_PATTERN.matcher(sanitized).find()) {
            log.error("Invalid characters in file name: {}", fileName);
            throw new StorageException("Invalid file name: contains illegal characters");
        }

        // Check for reserved names
        if (isReservedName(sanitized)) {
            log.error("Reserved file name: {}", sanitized);
            throw new StorageException("Invalid file name: uses reserved name");
        }

        // Check for leading/trailing dots or spaces
        if (sanitized.startsWith(".") || sanitized.endsWith(".") ||
                sanitized.startsWith(" ") || sanitized.endsWith(" ")) {
            log.warn("File name has leading/trailing dots or spaces: {}", fileName);
            sanitized = sanitized.replaceAll("^\\.+|\\.+$|^ +| +$", "");
        }

        if (sanitized.isEmpty()) {
            throw new StorageException("File name is empty after sanitization");
        }

        return sanitized;
    }

    /**
     * Build a complete path from directory and file name
     *
     * @param directory Directory path
     * @param fileName  File name
     * @return Complete sanitized path
     */
    public static String buildPath(String directory, String fileName) {
        String sanitizedDir = sanitizeDirectory(directory);
        String sanitizedFile = sanitizeFileName(fileName);

        if (sanitizedDir.isEmpty()) {
            return sanitizedFile;
        }

        return sanitizedDir + "/" + sanitizedFile;
    }

    /**
     * Extract file name from path
     *
     * @param path File path
     * @return File name
     */
    private static String getFileName(String path) {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    /**
     * Check if name is a Windows reserved name
     *
     * @param name File or directory name
     * @return true if reserved
     */
    private static boolean isReservedName(String name) {
        // Remove extension
        String nameWithoutExt = name.contains(".") ?
                name.substring(0, name.lastIndexOf('.')) : name;

        for (String reserved : RESERVED_NAMES) {
            if (reserved.equalsIgnoreCase(nameWithoutExt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate that a path is safe
     *
     * @param path Path to validate
     * @return true if valid
     */
    public static boolean isValidPath(String path) {
        try {
            sanitizePath(path);
            return true;
        } catch (StorageException e) {
            return false;
        }
    }
}
