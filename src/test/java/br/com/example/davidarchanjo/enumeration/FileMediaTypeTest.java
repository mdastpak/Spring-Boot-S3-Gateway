package br.com.example.davidarchanjo.enumeration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileMediaType enum
 */
class FileMediaTypeTest {

    @Test
    void testFromFilename_PdfExtension_ReturnsApplicationPdf() {
        MediaType mediaType = FileMediaType.fromFilename("document.pdf");

        assertEquals(MediaType.APPLICATION_PDF, mediaType);
    }

    @Test
    void testFromFilename_JpgExtension_ReturnsImageJpeg() {
        MediaType mediaType = FileMediaType.fromFilename("photo.jpg");

        assertEquals(MediaType.IMAGE_JPEG, mediaType);
    }

    @Test
    void testFromFilename_JpegExtension_ReturnsImageJpeg() {
        MediaType mediaType = FileMediaType.fromFilename("image.jpeg");

        assertEquals(MediaType.IMAGE_JPEG, mediaType);
    }

    @Test
    void testFromFilename_PngExtension_ReturnsImagePng() {
        MediaType mediaType = FileMediaType.fromFilename("screenshot.png");

        assertEquals(MediaType.IMAGE_PNG, mediaType);
    }

    @Test
    void testFromFilename_TxtExtension_ReturnsTextPlain() {
        MediaType mediaType = FileMediaType.fromFilename("readme.txt");

        assertEquals(MediaType.TEXT_PLAIN, mediaType);
    }

    @Test
    void testFromFilename_UnknownExtension_ReturnsOctetStream() {
        MediaType mediaType = FileMediaType.fromFilename("archive.zip");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, mediaType);
    }

    @Test
    void testFromFilename_NoExtension_ReturnsOctetStream() {
        MediaType mediaType = FileMediaType.fromFilename("README");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, mediaType);
    }

    @Test
    void testFromFilename_EmptyString_ReturnsOctetStream() {
        MediaType mediaType = FileMediaType.fromFilename("");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, mediaType);
    }

    @Test
    void testFromFilename_OnlyDot_ReturnsOctetStream() {
        MediaType mediaType = FileMediaType.fromFilename(".");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, mediaType);
    }

    @Test
    void testFromFilename_MultipleDotsInName_UsesLastExtension() {
        MediaType mediaType = FileMediaType.fromFilename("archive.tar.pdf");

        assertEquals(MediaType.APPLICATION_PDF, mediaType);
    }

    @ParameterizedTest
    @CsvSource({
            "file.pdf, APPLICATION_PDF",
            "image.jpg, IMAGE_JPEG",
            "photo.jpeg, IMAGE_JPEG",
            "icon.png, IMAGE_PNG",
            "notes.txt, TEXT_PLAIN"
    })
    void testFromFilename_SupportedExtensions(String filename, String expectedType) {
        MediaType mediaType = FileMediaType.fromFilename(filename);
        String mediaTypeString = expectedType.replace('_', '/');
        MediaType expected = MediaType.parseMediaType(mediaTypeString);

        assertEquals(expected, mediaType);
    }

    @Test
    void testFromFilename_CaseSensitive_UnknownIfUpperCase() {
        // Extensions are case-sensitive, uppercase should return default
        MediaType mediaType = FileMediaType.fromFilename("document.PDF");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, mediaType);
    }

    @Test
    void testFromFilename_ComplexPath_ExtractsExtension() {
        MediaType mediaType = FileMediaType.fromFilename("path/to/file/document.pdf");

        assertEquals(MediaType.APPLICATION_PDF, mediaType);
    }

    @Test
    void testFromFilename_PathWithMultipleDots_UsesLastExtension() {
        MediaType mediaType = FileMediaType.fromFilename("path.to.file/document.backup.txt");

        assertEquals(MediaType.TEXT_PLAIN, mediaType);
    }

    @Test
    void testEnumValues_HasCorrectExtensions() {
        assertEquals("jpg", FileMediaType.JPG.getExtension());
        assertEquals("jpeg", FileMediaType.JPEG.getExtension());
        assertEquals("png", FileMediaType.PNG.getExtension());
        assertEquals("pdf", FileMediaType.PDF.getExtension());
        assertEquals("txt", FileMediaType.TXT.getExtension());
    }

    @Test
    void testEnumValues_HasCorrectMediaTypes() {
        assertEquals(MediaType.IMAGE_JPEG, FileMediaType.JPG.getMediaType());
        assertEquals(MediaType.IMAGE_JPEG, FileMediaType.JPEG.getMediaType());
        assertEquals(MediaType.IMAGE_PNG, FileMediaType.PNG.getMediaType());
        assertEquals(MediaType.APPLICATION_PDF, FileMediaType.PDF.getMediaType());
        assertEquals(MediaType.TEXT_PLAIN, FileMediaType.TXT.getMediaType());
    }

    @Test
    void testEnumValues_Count() {
        FileMediaType[] values = FileMediaType.values();

        assertEquals(5, values.length);
    }

    @Test
    void testFromFilename_DotAtStart_ReturnsOctetStream() {
        MediaType mediaType = FileMediaType.fromFilename(".hidden");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, mediaType);
    }

    @Test
    void testFromFilename_HiddenFileWithExtension_ReturnsCorrectType() {
        MediaType mediaType = FileMediaType.fromFilename(".hidden.pdf");

        assertEquals(MediaType.APPLICATION_PDF, mediaType);
    }
}
