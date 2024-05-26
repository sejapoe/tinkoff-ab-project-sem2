package ru.sejapoe.tinkab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        String accessKey,
        String secretKey,
        String bucketName,
        String endpoint,
        String region
) {
}