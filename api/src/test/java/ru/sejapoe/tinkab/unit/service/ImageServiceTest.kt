package ru.sejapoe.tinkab.unit.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.mock.web.MockMultipartFile
import ru.sejapoe.tinkab.domain.ImageEntity
import ru.sejapoe.tinkab.domain.UserEntity
import ru.sejapoe.tinkab.exception.NotFoundException
import ru.sejapoe.tinkab.repo.image.ImageRepository
import ru.sejapoe.tinkab.service.ImageService
import ru.sejapoe.tinkab.service.UserService
import ru.sejapoe.tinkab.service.storage.StorageService
import java.io.InputStream
import java.util.*

class ImageServiceTest {

    @Test
    @DisplayName("Test save image")
    fun saveImage() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")

        val storageService = mock<StorageService> {
            on { store(any()) } doReturn uuid
        }

        val imageRepository = mock<ImageRepository> {
            on { save(any()) } doAnswer {}
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(storageService, imageRepository, userService)
        val file = MockMultipartFile("image.png", "image.png", "image/png", InputStream.nullInputStream())
        val imageEntity = imageService.saveImage(file)

        verify(storageService, times(1)).store(file)
        verify(imageRepository, times(1)).save(imageEntity)
        assertEquals(uuid, imageEntity.id())
        assertEquals(userEntity.id(), imageEntity.userId())
        assertEquals(file.originalFilename, imageEntity.filename())
    }

    @Test
    @DisplayName("Test load all images of current user")
    fun getAll() {
        val userEntity = UserEntity(1L, "user", "password")

        val imageRepository = mock<ImageRepository> {
            on { getByUserId(userEntity.id()) } doReturn listOf(
                ImageEntity(UUID.randomUUID(), "image.png", 128000L, 1L),
                ImageEntity(UUID.randomUUID(), "image1.png", 128000L, 1L),
                ImageEntity(UUID.randomUUID(), "files/image.jpeg", 128000L, 1L),
                ImageEntity(UUID.randomUUID(), "image2.jpeg", 128000L, 1L)
            )
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        val imageEntities = imageService.getAll()

        verify(imageRepository, times(1)).getByUserId(userEntity.id())
        assertThat(imageEntities).hasSize(4)
        assertTrue(imageEntities.all { it.userId() == userEntity.id() })
    }

    @Test
    @DisplayName("Test get image by UUID")
    fun getById() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")
        val imageEntity = ImageEntity(uuid, "image.png", 128000L, userEntity.id())

        val imageRepository = mock<ImageRepository> {
            on { get(uuid) } doReturn Optional.of(imageEntity)
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        val actualImageEntity = imageService.getById(uuid)

        verify(imageRepository, times(1)).get(uuid)
        assertEquals(imageEntity, actualImageEntity)
    }

    @Test
    @DisplayName("Test get not existing image by UUID")
    fun getByIdNotFound() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")

        val imageRepository = mock<ImageRepository> {
            on { get(uuid) } doReturn Optional.empty()
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        assertThrows<NotFoundException> {
            imageService.getById(uuid)
        }

        verify(imageRepository, times(1)).get(uuid)
    }

    @Test
    @DisplayName("Test get image by UUID, but owned by other user")
    fun getByIdForbidden() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")
        val imageEntity = ImageEntity(uuid, "image.png", 128000L, userEntity.id() + 100)

        val imageRepository = mock<ImageRepository> {
            on { get(uuid) } doReturn Optional.of(imageEntity)
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        assertThrows<NotFoundException> {
            imageService.getById(uuid)
        }

        verify(imageRepository, times(1)).get(uuid)
    }

    @Test
    @DisplayName("Test delete image by UUID")
    fun deleteById() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")
        val imageEntity = ImageEntity(uuid, "image.png", 128000L, userEntity.id())

        val imageRepository = mock<ImageRepository> {
            on { get(uuid) } doReturn Optional.of(imageEntity)
            on { remove(uuid) } doAnswer {}
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        imageService.deleteImage(uuid)

        verify(imageRepository, times(1)).get(uuid)
        verify(imageRepository, times(1)).remove(uuid)
    }

    @Test
    @DisplayName("Test delete not existing image by UUID")
    fun deleteByIdNotFound() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")

        val imageRepository = mock<ImageRepository> {
            on { get(uuid) } doReturn Optional.empty()
            on { remove(uuid) } doAnswer {}
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        assertThrows<NotFoundException> {
            imageService.deleteImage(uuid)
        }

        verify(imageRepository, times(1)).get(uuid)
        verify(imageRepository, times(0)).remove(uuid)
    }

    @Test
    @DisplayName("Test delete image by UUID, but owned by other user")
    fun deleteByIdForbidden() {
        val uuid = UUID.fromString("55e1d48d-009a-499e-95ff-44222897303c")
        val userEntity = UserEntity(1L, "user", "password")
        val imageEntity = ImageEntity(uuid, "image.png", 128000L, userEntity.id() + 100)

        val imageRepository = mock<ImageRepository> {
            on { get(uuid) } doReturn Optional.of(imageEntity)
            on { remove(uuid) } doAnswer {}
        }

        val userService = mock<UserService> {
            on { loadCurrentUser() } doReturn userEntity
        }

        val imageService = ImageService(mock(), imageRepository, userService)
        assertThrows<NotFoundException> {
            imageService.getById(uuid)
        }

        verify(imageRepository, times(1)).get(uuid)
        verify(imageRepository, times(0)).remove(uuid)
    }
}
