package br.com.example.davidarchanjo.controller;

import br.com.example.davidarchanjo.dto.FileListResponse;
import br.com.example.davidarchanjo.dto.FileUploadResponse;
import br.com.example.davidarchanjo.enumeration.FileMediaType;
import br.com.example.davidarchanjo.service.S3BucketStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage")
@Tag(name = "Object Storage", description = "Object storage operations API (S3/MinIO compatible)")
public class S3BucketStorageController {

    private final S3BucketStorageService service;

    @Operation(
            summary = "List files in bucket (Public)",
            description = "Retrieve all files from a specified bucket. " +
                    "This endpoint is public and does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files listed successfully",
                    content = @Content(schema = @Schema(implementation = FileListResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bucket not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{bucketName}")
    public ResponseEntity<FileListResponse> listFiles(
            @Parameter(description = "Bucket name", example = "my-bucket")
            @PathVariable("bucketName") @NotBlank String bucketName
    ) {
        log.info("Listing files in bucket: {}", bucketName);
        List<String> files = service.listFiles(bucketName);

        FileListResponse response = FileListResponse.builder()
                .bucketName(bucketName)
                .files(files)
                .totalFiles(files.size())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Upload file (Public)",
            description = "Upload a file to the specified bucket. " +
                    "This endpoint is public and does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Bucket not found"),
            @ApiResponse(responseCode = "413", description = "File size exceeds limit"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping(value = "/{bucketName}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "Bucket name", example = "my-bucket")
            @PathVariable("bucketName") @NotBlank String bucketName,

            @Parameter(description = "File to upload", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "Custom file name", example = "document.pdf", required = true)
            @RequestPart("fileName") @NotBlank String fileName
    ) throws IOException {
        log.info("Uploading file '{}' to bucket '{}'", fileName, bucketName);

        service.uploadFile(
                bucketName,
                fileName,
                file.getSize(),
                file.getContentType(),
                file.getInputStream()
        );

        FileUploadResponse response = FileUploadResponse.builder()
                .message("File uploaded successfully")
                .fileName(fileName)
                .bucketName(bucketName)
                .fileSize(file.getSize())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Download file (Public)",
            description = "Download a file from the specified bucket. " +
                    "This endpoint is public and does not require authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "File or bucket not found"),
            @ApiResponse(responseCode = "500", description = "Download failed")
    })
    @GetMapping("/{bucketName}/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "Bucket name", example = "my-bucket")
            @PathVariable("bucketName") @NotBlank String bucketName,

            @Parameter(description = "File name to download", example = "document.pdf")
            @PathVariable("fileName") @NotBlank String fileName
    ) {
        log.info("Downloading file '{}' from bucket '{}'", fileName, bucketName);
        ByteArrayOutputStream fileContent = service.downloadFile(bucketName, fileName);
        MediaType contentType = FileMediaType.fromFilename(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(contentType)
                .body(fileContent.toByteArray());
    }

    @Operation(
            summary = "Delete file (Public)",
            description = "Delete a file from the specified bucket. " +
                    "This endpoint is public and does not require authentication. " +
                    "Use force=true to bypass additional checks."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "File or bucket not found"),
            @ApiResponse(responseCode = "500", description = "Delete failed")
    })
    @DeleteMapping("/{bucketName}/{fileName}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Bucket name", example = "my-bucket")
            @PathVariable("bucketName") @NotBlank String bucketName,

            @Parameter(description = "File name to delete", example = "document.pdf")
            @PathVariable("fileName") @NotBlank String fileName,

            @Parameter(description = "Force delete - bypass additional checks", example = "false")
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) {
        if (force) {
            log.info("Force deleting file '{}' from bucket '{}' (bypassing checks)", fileName, bucketName);
        } else {
            log.info("Deleting file '{}' from bucket '{}'", fileName, bucketName);
        }

        service.deleteFile(bucketName, fileName);
        return ResponseEntity.noContent().build();
    }
}

