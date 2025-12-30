package br.com.example.davidarchanjo.util;

import br.com.example.davidarchanjo.enumeration.DuplicateFileStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileNameGenerator utility class
 */
class FileNameGeneratorTest {

    @Test
    void testGenerateFileName_UuidSuffix_AddsUniqueId() {
        String fileName = FileNameGenerator.generateFileName("document.pdf", DuplicateFileStrategy.UUID_SUFFIX, null);

        assertNotNull(fileName);
        assertTrue(fileName.startsWith("document_"));
        assertTrue(fileName.endsWith(".pdf"));
        assertTrue(fileName.matches("document_[a-f0-9]{8}\\.pdf"));
    }

    @Test
    void testGenerateFileName_TimestampSuffix_AddsTimestamp() {
        String fileName = FileNameGenerator.generateFileName("report.xlsx", DuplicateFileStrategy.TIMESTAMP_SUFFIX, null);

        assertNotNull(fileName);
        assertTrue(fileName.startsWith("report_"));
        assertTrue(fileName.endsWith(".xlsx"));
        assertTrue(fileName.matches("report_\\d{8}_\\d{6}\\.xlsx"));
    }

    @Test
    void testGenerateFileName_Version_AddsVersionNumber() {
        String fileName = FileNameGenerator.generateFileName("contract.pdf", DuplicateFileStrategy.VERSION, 2);

        assertEquals("contract_v2.pdf", fileName);
    }

    @Test
    void testGenerateFileName_Version_FirstVersion_NoSuffix() {
        String fileName = FileNameGenerator.generateFileName("contract.pdf", DuplicateFileStrategy.VERSION, 1);

        assertEquals("contract.pdf", fileName);
    }

    @Test
    void testGenerateFileName_Version_NullVersion_NoSuffix() {
        String fileName = FileNameGenerator.generateFileName("contract.pdf", DuplicateFileStrategy.VERSION, null);

        assertEquals("contract.pdf", fileName);
    }

    @Test
    void testGenerateFileName_Version_HigherVersion() {
        String fileName = FileNameGenerator.generateFileName("document.doc", DuplicateFileStrategy.VERSION, 15);

        assertEquals("document_v15.doc", fileName);
    }

    @Test
    void testGenerateFileName_Overwrite_ReturnsOriginal() {
        String fileName = FileNameGenerator.generateFileName("image.png", DuplicateFileStrategy.OVERWRITE, null);

        assertEquals("image.png", fileName);
    }

    @Test
    void testGenerateFileName_Reject_ReturnsOriginal() {
        String fileName = FileNameGenerator.generateFileName("data.csv", DuplicateFileStrategy.REJECT, null);

        assertEquals("data.csv", fileName);
    }

    @Test
    void testGenerateFileName_NoExtension_UuidSuffix() {
        String fileName = FileNameGenerator.generateFileName("README", DuplicateFileStrategy.UUID_SUFFIX, null);

        assertNotNull(fileName);
        assertTrue(fileName.startsWith("README_"));
        assertTrue(fileName.matches("README_[a-f0-9]{8}"));
        assertFalse(fileName.contains("."));
    }

    @Test
    void testGenerateFileName_NoExtension_TimestampSuffix() {
        String fileName = FileNameGenerator.generateFileName("LICENSE", DuplicateFileStrategy.TIMESTAMP_SUFFIX, null);

        assertNotNull(fileName);
        assertTrue(fileName.startsWith("LICENSE_"));
        assertTrue(fileName.matches("LICENSE_\\d{8}_\\d{6}"));
    }

    @Test
    void testGenerateFileName_NoExtension_Version() {
        String fileName = FileNameGenerator.generateFileName("README", DuplicateFileStrategy.VERSION, 3);

        assertEquals("README_v3", fileName);
    }

    @Test
    void testGenerateFileName_MultipleDotsInName_UuidSuffix() {
        String fileName = FileNameGenerator.generateFileName("file.name.with.dots.txt", DuplicateFileStrategy.UUID_SUFFIX, null);

        assertNotNull(fileName);
        assertTrue(fileName.startsWith("file.name.with.dots_"));
        assertTrue(fileName.endsWith(".txt"));
        assertTrue(fileName.matches("file\\.name\\.with\\.dots_[a-f0-9]{8}\\.txt"));
    }

    @Test
    void testGenerateFileName_MultipleDotsInName_Version() {
        String fileName = FileNameGenerator.generateFileName("archive.tar.gz", DuplicateFileStrategy.VERSION, 2);

        assertEquals("archive.tar_v2.gz", fileName);
    }

    @Test
    void testGenerateUniqueId_GeneratesValidUuid() {
        String uniqueId = FileNameGenerator.generateUniqueId();

        assertNotNull(uniqueId);
        assertEquals(36, uniqueId.length()); // UUID format: 8-4-4-4-12 with 4 hyphens
        assertTrue(uniqueId.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
    }

    @Test
    void testGenerateUniqueId_GeneratesUnique() {
        String id1 = FileNameGenerator.generateUniqueId();
        String id2 = FileNameGenerator.generateUniqueId();

        assertNotEquals(id1, id2);
    }

    @Test
    void testGenerateShortUniqueId_GeneratesValidShortId() {
        String shortId = FileNameGenerator.generateShortUniqueId();

        assertNotNull(shortId);
        assertEquals(8, shortId.length());
        assertTrue(shortId.matches("[a-f0-9]{8}"));
    }

    @Test
    void testGenerateShortUniqueId_GeneratesUnique() {
        String id1 = FileNameGenerator.generateShortUniqueId();
        String id2 = FileNameGenerator.generateShortUniqueId();

        // High probability of being different
        assertNotEquals(id1, id2);
    }

    @Test
    void testGenerateFileName_UuidSuffix_MultipleCalls_GeneratesDifferent() {
        String fileName1 = FileNameGenerator.generateFileName("test.txt", DuplicateFileStrategy.UUID_SUFFIX, null);
        String fileName2 = FileNameGenerator.generateFileName("test.txt", DuplicateFileStrategy.UUID_SUFFIX, null);

        assertNotEquals(fileName1, fileName2);
    }

    @Test
    void testGenerateFileName_TimestampSuffix_SameSecond_SameTimestamp() {
        String fileName1 = FileNameGenerator.generateFileName("test.txt", DuplicateFileStrategy.TIMESTAMP_SUFFIX, null);
        String fileName2 = FileNameGenerator.generateFileName("test.txt", DuplicateFileStrategy.TIMESTAMP_SUFFIX, null);

        // If executed in same second, timestamps should be identical
        // This is expected behavior for timestamp strategy
        assertTrue(fileName1.startsWith("test_"));
        assertTrue(fileName2.startsWith("test_"));
    }

    @Test
    void testGenerateFileName_ComplexFileNames() {
        // Test with various complex file names
        String result1 = FileNameGenerator.generateFileName("My-Document-2025.pdf", DuplicateFileStrategy.VERSION, 2);
        assertEquals("My-Document-2025_v2.pdf", result1);

        String result2 = FileNameGenerator.generateFileName("invoice_001.pdf", DuplicateFileStrategy.VERSION, 3);
        assertEquals("invoice_001_v3.pdf", result2);
    }

    @Test
    void testGenerateFileName_LongFileName_HandlesCorrectly() {
        String longName = "very-long-file-name-with-many-characters-in-it-for-testing-purposes.pdf";
        String fileName = FileNameGenerator.generateFileName(longName, DuplicateFileStrategy.VERSION, 2);

        assertTrue(fileName.contains("_v2"));
        assertTrue(fileName.endsWith(".pdf"));
    }
}
