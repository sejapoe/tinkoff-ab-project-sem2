package ru.sejapoe.tinkab.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String bucketName,
        @NotBlank String endpoint,
        @NotBlank String region
) {
}