package br.com.example.davidarchanjo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "File upload request")
public class FileUploadRequest {

    @NotNull(message = "File is required")
    @Schema(description = "File to upload", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    @NotBlank(message = "File name is required")
    @Schema(description = "Custom file name in storage", example = "document.pdf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;
}
