package br.com.example.davidarchanjo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public download information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Public download information")
public class PublicDownloadResponse {

    @Schema(description = "File name", example = "document.pdf")
    private String fileName;

    @Schema(description = "File size in bytes", example = "1024")
    private Long fileSize;

    @Schema(description = "Content type", example = "application/pdf")
    private String contentType;

    @Schema(description = "Direct download URL (if applicable)")
    private String downloadUrl;
}
