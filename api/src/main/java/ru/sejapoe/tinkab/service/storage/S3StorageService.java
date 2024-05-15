package ru.sejapoe.tinkab.service.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sejapoe.tinkab.config.StorageProperties;
import ru.sejapoe.tinkab.exception.NotFoundException;
import ru.sejapoe.tinkab.exception.StorageException;

import java.io.IOException;
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

        log.info("S3 storage has successfully initialized");
    }

    @SneakyThrows
    @Override
    public UUID store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }


            UUID uuid = UUID.randomUUID();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uuid.toString())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return uuid;
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    public void remove(UUID uuid) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uuid.toString())
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to remove file", e);
        }
    }

    @SneakyThrows
    @Override
    public Resource loadAsResource(String uuid) {
        try {
            var bytes = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uuid)
                            .build()
            ).readAllBytes();

            return new ByteArrayResource(bytes);
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
    public long getSize(UUID uuid) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(String.valueOf(uuid))
                            .build()
            ).size();
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new NotFoundException("Failed to read file: " + uuid);
            } else {
                throw e;
            }
        }
    }

}