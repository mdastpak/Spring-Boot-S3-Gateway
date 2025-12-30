package br.com.example.davidarchanjo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "File list response")
public class FileListResponse {

    @Schema(description = "Bucket name", example = "my-bucket")
    private String bucketName;

    @Schema(description = "List of file names")
    private List<String> files;

    @Schema(description = "Total number of files", example = "10")
    private Integer totalFiles;
}
