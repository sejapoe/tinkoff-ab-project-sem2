package ru.sejapoe.tinkab.unit.service

import io.minio.GetObjectResponse
import io.minio.MinioClient
import io.minio.errors.ErrorResponseException
import io.minio.messages.ErrorResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.mock.web.MockMultipartFile
import ru.sejapoe.tinkab.config.StorageProperties
import ru.sejapoe.tinkab.exception.NotFoundException
import ru.sejapoe.tinkab.exception.StorageException
import ru.sejapoe.tinkab.service.storage.S3StorageService
import java.io.IOException
import java.io.InputStream
import java.util.*

class S3StorageServiceTest {

    @Test
    @DisplayName("Test init minio, create bucket")
    fun init() {
        val storageProperties = mock<StorageProperties> {
            on { bucketName } doReturn "test-bucket"
        }

        val minioClient = mock<MinioClient> {
            on { bucketExists(any()) } doReturn false
            on { makeBucket(any()) } doAnswer {}
        }

        val s3StorageService = S3StorageService(storageProperties, minioClient)
        s3StorageService.init()

        verify(minioClient, times(1)).bucketExists(any())
        verify(minioClient, times(1)).makeBucket(any())
    }

    @Test
    @DisplayName("Test init minio, bucket exists")
    fun initBucketExists() {
        val storageProperties = mock<StorageProperties> {
            on { bucketName } doReturn "test-bucket"
        }

        val minioClient = mock<MinioClient> {
            on { bucketExists(any()) } doReturn true
            on { makeBucket(any()) } doAnswer {}
        }

        val s3StorageService = S3StorageService(storageProperties, minioClient)
        s3StorageService.init()

        verify(minioClient, times(1)).bucketExists(any())
        verify(minioClient, times(0)).makeBucket(any())
    }

    @Test
    @DisplayName("Test init minio, bad bucket name")
    fun initBadBucketName() {
        val storageProperties = mock<StorageProperties> {
            on { bucketName } doReturn ""
        }

        val minioClient = mock<MinioClient>()

        val s3StorageService = S3StorageService(storageProperties, minioClient)
        assertThrows<StorageException> {
            s3StorageService.init()
        }

        verify(minioClient, times(0)).bucketExists(any())
        verify(minioClient, times(0)).makeBucket(any())
    }

    @Test
    @DisplayName("Test store file")
    fun store() {
        val minioClient = mock<MinioClient> {
            on { putObject(any()) } doReturn null
        }

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        val multipartFile = MockMultipartFile("image.png", "abracadabra".toByteArray())
        s3StorageService.store(multipartFile)

        verify(minioClient, times(1)).putObject(any())
    }

    @Test
    @DisplayName("Test store empty file")
    fun storeEmptyFile() {
        val minioClient = mock<MinioClient>()

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        val multipartFile = MockMultipartFile("image.png", InputStream.nullInputStream())
        assertThrows<StorageException> {
            s3StorageService.store(multipartFile)
        }

        verify(minioClient, times(0)).putObject(any())
    }

    @Test
    @DisplayName("Test store file failed")
    fun storeFileFailed() {
        val minioClient = mock<MinioClient> {
            on { putObject(any()) } doThrow IOException()
        }

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        val multipartFile = MockMultipartFile("image.png", "abracadabra".toByteArray())
        assertThrows<StorageException> {
            s3StorageService.store(multipartFile)
        }

        verify(minioClient, times(1)).putObject(any())
    }

    @Test
    @DisplayName("Test remove file")
    fun remove() {
        val minioClient = mock<MinioClient> {
            on { removeObject(any()) } doAnswer {}
        }

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        s3StorageService.remove(UUID.randomUUID())

        verify(minioClient, times(1)).removeObject(any())
    }

    @Test
    @DisplayName("Test remove file failed")
    fun removeFailed() {
        val minioClient = mock<MinioClient> {
            on { removeObject(any()) } doThrow IOException()
        }

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        assertThrows<StorageException> {
            s3StorageService.remove(UUID.randomUUID())
        }

        verify(minioClient, times(1)).removeObject(any())
    }

    @Test
    fun loadAsResource() {
        val getObjectResponse = mock<GetObjectResponse> {
            on { readAllBytes() } doReturn "abracadabra".toByteArray()
        }

        val minioClient = mock<MinioClient> {
            on { getObject(any()) } doReturn getObjectResponse
        }

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        val resource = s3StorageService.loadAsResource(UUID.randomUUID().toString())
        assertEquals("abracadabra", resource.inputStream.bufferedReader().use { it.readText() })
        verify(minioClient, times(1)).getObject(any())
    }

    @Test
    fun loadAsResourceNoSuchKey() {
        val errorResponse = mock<ErrorResponse> {
            on { code() } doReturn "NoSuchKey"
        }

        val errorResponseException = mock<ErrorResponseException> {
            on { errorResponse() } doReturn errorResponse
        }

        val minioClient = mock<MinioClient> {
            on { getObject(any()) } doThrow errorResponseException
        }

        val s3StorageService = S3StorageService(mock(), minioClient).apply {
            val bucketNameField = S3StorageService::class.java.getDeclaredField("bucketName")
            bucketNameField.isAccessible = true
            bucketNameField.set(this, "test-bucket")
        }

        assertThrows<NotFoundException> {
            s3StorageService.loadAsResource(UUID.randomUUID().toString())
        }

        verify(minioClient, times(1)).getObject(any())
    }
}
