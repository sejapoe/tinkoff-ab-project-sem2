package ru.sejapoe.tinkab.config;


import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {
    @Bean
    public MinioClient minioClient(StorageProperties storageProperties) {
        return MinioClient.builder()
                .credentials(storageProperties.accessKey(), storageProperties.secretKey())
                .region(storageProperties.region())
                .endpoint(storageProperties.endpoint())
                .build();
    }
}
