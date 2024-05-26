package ru.sejapoe.tinkab.service.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sejapoe.tinkab.config.StorageProperties;
import ru.sejapoe.tinkab.exception.NotFoundException;
import ru.sejapoe.tinkab.exception.StorageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final StorageProperties storageProperties;
    private final MinioClient minioClient;
    private String bucketName;

    @SneakyThrows
    @PostConstruct
    @Override
    public void init() {
        bucketName = storageProperties.bucketName();

        if (Objects.isNull(bucketName) || bucketName.isBlank()) {
            throw new StorageException("You should specify bucket name to use S3 storage");
        }

        boolean doesBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!doesBucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
        }

        minioClient.setBucketLifecycle(SetBucketLifecycleArgs.builder()
                .bucket(bucketName)
                .config(new LifecycleConfiguration(
                        List.of(
                                new LifecycleRule(
                                        Status.ENABLED,
                                        null,
                                        // todo: move days to properties
                                        new Expiration((ResponseDate) null, 5, null),
                                        new RuleFilter(new AndOperator(
                                                "",
                                                Map.of("isTemp", String.valueOf(true))
                                        )),
                                        "TemporaryImagesExpirationRule",
                                        null,
                                        null,
                                        null
                                )
                        )
                ))
                .build());

        log.info("S3 storage has successfully initialized");
    }

    @SneakyThrows
    @Override
    public UUID store(byte[] bytes, String contentType, boolean isTemp) {
        try {
            if (bytes.length == 0) {
                throw new StorageException("Failed to store empty file");
            }


            UUID uuid = UUID.randomUUID();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uuid.toString())
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType(contentType)
                            .tags(Map.of("isTemp", Boolean.toString(isTemp)))
                            .build()
            );

            return uuid;
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @SneakyThrows
    @Override
    public byte[] loadAsBytes(UUID uuid) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uuid.toString())
                            .build()
            ).readAllBytes();
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new NotFoundException("Failed to read file: " + uuid);
            } else {
                throw e;
            }
        }
    }

    @SneakyThrows
    @Override
    public String getContentType(UUID uuid) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(String.valueOf(uuid))
                            .build()
            ).contentType();
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new NotFoundException("Failed to read file: " + uuid);
            } else {
                throw e;
            }
        }
    }

}