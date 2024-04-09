package ru.sejapoe.tinkab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.sejapoe.tinkab.domain.ImageEntity;
import ru.sejapoe.tinkab.domain.UserEntity;
import ru.sejapoe.tinkab.exception.NotFoundException;
import ru.sejapoe.tinkab.repo.image.ImageRepository;
import ru.sejapoe.tinkab.service.storage.StorageService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private final UserService userService;

    @Transactional
    public ImageEntity saveImage(MultipartFile file) {
        UserEntity currentUser = userService.loadCurrentUser();
        UUID uuid = storageService.store(file);
        ImageEntity imageEntity = new ImageEntity(uuid, file.getOriginalFilename(), file.getSize(), currentUser.id());
        imageRepository.save(imageEntity);
        return imageEntity;
    }

    @Transactional
    public List<ImageEntity> getAll() {
        UserEntity currentUser = userService.loadCurrentUser();
        return imageRepository.getByUserId(currentUser.id());
    }

    @Transactional
    public ImageEntity getById(UUID imageId) {
        ImageEntity imageEntity = imageRepository.get(imageId).orElseThrow(() ->
                new NotFoundException("Image [%s] is not found".formatted(imageId))
        );
        checkRights(imageEntity);
        return imageEntity;
    }

    @Transactional
    public void deleteImage(UUID imageId) {
        ImageEntity imageEntity = imageRepository.get(imageId).orElseThrow(() ->
                new NotFoundException("Image [%s] is not found".formatted(imageId))
        );
        checkRights(imageEntity);
        imageRepository.remove(imageId);
        storageService.remove(imageId);
    }

    private void checkRights(ImageEntity imageEntity) {
        UserEntity currentUser = userService.loadCurrentUser();
        if (!Objects.equals(imageEntity.userId(), currentUser.id())) {
            throw new NotFoundException("Image [%s] is not found".formatted(imageEntity.id()));
        }
    }
}
