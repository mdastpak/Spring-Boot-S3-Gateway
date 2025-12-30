package br.com.example.davidarchanjo.util;

import br.com.example.davidarchanjo.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PathSanitizer utility class
 */
class PathSanitizerTest {

    @Test
    void testSanitizePath_ValidPath() {
        String result = PathSanitizer.sanitizePath("documents/file.pdf");
        assertEquals("documents/file.pdf", result);
    }

    @Test
    void testSanitizePath_ValidNestedPath() {
        String result = PathSanitizer.sanitizePath("2025/invoices/invoice-001.pdf");
        assertEquals("2025/invoices/invoice-001.pdf", result);
    }

    @Test
    void testSanitizePath_AbsolutePathWithLeadingSlash_ThrowsException() {
        // Leading slash indicates absolute path which should be blocked
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath("/documents/file.pdf")
        );
        assertTrue(exception.getMessage().contains("absolute"));
    }

    @Test
    void testSanitizePath_NormalizesBackslashes() {
        String result = PathSanitizer.sanitizePath("documents\\file.pdf");
        assertEquals("documents/file.pdf", result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../../../etc/passwd",
            "../../passwords.txt",
            "docs/../../../sensitive",
            "folder/../../etc/passwd"
    })
    void testSanitizePath_BlocksPathTraversal(String maliciousPath) {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath(maliciousPath)
        );
        assertTrue(exception.getMessage().contains("path traversal"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/etc/passwd",
            "/absolute/path",
            "C:\\Windows\\System32",
            "C:/Windows/System32",
            "/usr/local/bin"
    })
    void testSanitizePath_BlocksAbsolutePaths(String absolutePath) {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath(absolutePath)
        );
        assertTrue(exception.getMessage().contains("absolute"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "file<name>.txt",
            "file>name.txt",
            "file:name.txt",
            "file|name.txt",
            "file?name.txt",
            "file*name.txt",
            "file\"name.txt"
    })
    void testSanitizePath_BlocksInvalidCharacters(String invalidPath) {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath(invalidPath)
        );
        assertTrue(exception.getMessage().contains("illegal characters"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CON.txt",
            "PRN.doc",
            "AUX.pdf",
            "NUL.file",
            "COM1.txt",
            "LPT1.doc"
    })
    void testSanitizePath_BlocksReservedNames(String reservedName) {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath(reservedName)
        );
        assertTrue(exception.getMessage().contains("reserved name"));
    }

    @Test
    void testSanitizePath_NullPath_ThrowsException() {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath(null)
        );
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void testSanitizePath_EmptyPath_ThrowsException() {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath("")
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testSanitizePath_WhitespacePath_ThrowsException() {
        StorageException exception = assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizePath("   ")
        );
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    void testSanitizeDirectory_ValidDirectory() {
        String result = PathSanitizer.sanitizeDirectory("documents/invoices");
        assertEquals("documents/invoices", result);
    }

    @Test
    void testSanitizeDirectory_RemovesTrailingSlash() {
        String result = PathSanitizer.sanitizeDirectory("documents/invoices/");
        assertEquals("documents/invoices", result);
    }

    @Test
    void testSanitizeDirectory_NullOrEmpty_ReturnsEmpty() {
        assertEquals("", PathSanitizer.sanitizeDirectory(null));
        assertEquals("", PathSanitizer.sanitizeDirectory(""));
        assertEquals("", PathSanitizer.sanitizeDirectory("   "));
    }

    @Test
    void testSanitizeFileName_ValidFileName() {
        String result = PathSanitizer.sanitizeFileName("document.pdf");
        assertEquals("document.pdf", result);
    }

    @Test
    void testSanitizeFileName_FileNameWithDashes() {
        String result = PathSanitizer.sanitizeFileName("my-document-2025.pdf");
        assertEquals("my-document-2025.pdf", result);
    }

    @Test
    void testSanitizeFileName_ContainsPathSeparator_ThrowsException() {
        assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizeFileName("folder/file.pdf")
        );
        assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizeFileName("folder\\file.pdf")
        );
    }

    @Test
    void testSanitizeFileName_RemovesLeadingTrailingDots() {
        String result = PathSanitizer.sanitizeFileName("..file.pdf");
        assertEquals("file.pdf", result);
    }

    @Test
    void testSanitizeFileName_RemovesLeadingTrailingSpaces() {
        String result = PathSanitizer.sanitizeFileName("  file.pdf  ");
        assertEquals("file.pdf", result);
    }

    @Test
    void testSanitizeFileName_ReservedName_ThrowsException() {
        assertThrows(StorageException.class, () ->
                PathSanitizer.sanitizeFileName("CON.txt")
        );
    }

    @Test
    void testBuildPath_ValidInputs() {
        String result = PathSanitizer.buildPath("documents", "file.pdf");
        assertEquals("documents/file.pdf", result);
    }

    @Test
    void testBuildPath_EmptyDirectory() {
        String result = PathSanitizer.buildPath("", "file.pdf");
        assertEquals("file.pdf", result);
    }

    @Test
    void testBuildPath_NullDirectory() {
        String result = PathSanitizer.buildPath(null, "file.pdf");
        assertEquals("file.pdf", result);
    }

    @Test
    void testBuildPath_NestedDirectory() {
        String result = PathSanitizer.buildPath("2025/invoices", "invoice-001.pdf");
        assertEquals("2025/invoices/invoice-001.pdf", result);
    }

    @Test
    void testIsValidPath_ValidPath_ReturnsTrue() {
        assertTrue(PathSanitizer.isValidPath("documents/file.pdf"));
        assertTrue(PathSanitizer.isValidPath("2025/invoices/invoice.pdf"));
    }

    @Test
    void testIsValidPath_InvalidPath_ReturnsFalse() {
        assertFalse(PathSanitizer.isValidPath("../../etc/passwd"));
        assertFalse(PathSanitizer.isValidPath("/absolute/path"));
        assertFalse(PathSanitizer.isValidPath("file<>name.txt"));
        assertFalse(PathSanitizer.isValidPath("CON.txt"));
    }

    @Test
    void testIsValidPath_NullPath_ReturnsFalse() {
        assertFalse(PathSanitizer.isValidPath(null));
    }

    @Test
    void testIsValidPath_EmptyPath_ReturnsFalse() {
        assertFalse(PathSanitizer.isValidPath(""));
    }
}
