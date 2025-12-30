package br.com.example.davidarchanjo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "File upload response")
public class FileUploadResponse {

    @Schema(description = "Upload status message", example = "File uploaded successfully")
    private String message;

    @Schema(description = "Uploaded file name", example = "document.pdf")
    private String fileName;

    @Schema(description = "Bucket name", example = "my-bucket")
    private String bucketName;

    @Schema(description = "File size in bytes", example = "1024")
    private Long fileSize;
}
