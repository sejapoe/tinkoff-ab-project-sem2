package ru.sejapoe.tinkab.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sejapoe.tinkab.domain.ImageEntity;
import ru.sejapoe.tinkab.domain.ImageFilter;
import ru.sejapoe.tinkab.domain.ImageFilterEntity;
import ru.sejapoe.tinkab.domain.ImageRequestStatus;
import ru.sejapoe.tinkab.exception.InternalServerError;
import ru.sejapoe.tinkab.exception.NotFoundException;
import ru.sejapoe.tinkab.kafka.message.ImageWipMessage;
import ru.sejapoe.tinkab.repo.image.ImageRepository;
import ru.sejapoe.tinkab.repo.image.filter.ImageFilterRepository;
import ru.sejapoe.tinkab.service.storage.S3StorageService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageFilterService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ImageFilterRepository imageFilterRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final S3StorageService s3StorageService;

    @Transactional
    public UUID applyFilters(UUID imageId, List<ImageFilter> filters) {
        // will throw error if image doesn't exist or not accessible by current user
        ImageEntity imageEntity = imageService.getById(imageId);

        ImageFilterEntity filterEntity = imageFilterRepository.create(imageEntity.id());

        var message = new ImageWipMessage(imageId, filterEntity.id(), filters.stream().map(Enum::name).toList());
        try {
            kafkaTemplate.send("images.wip", message).join();
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage());
        }
        return filterEntity.id();
    }


    @Transactional
    public ImageFilterEntity getById(UUID imageId, UUID requestId) {
        // will throw error if image doesn't exist or not accessible by current user
        imageService.getById(imageId);
        Optional<ImageFilterEntity> imageFilterEntity = imageFilterRepository.get(requestId);
        if (imageFilterEntity.isEmpty() || !imageFilterEntity.get().originalImageId().equals(imageId)) {
            throw new NotFoundException("Request [%s] is not found".formatted(requestId));
        }
        return imageFilterEntity.get();
    }

    @Transactional
    public void setDone(UUID requestId, UUID editedImageId) {
        var request = imageFilterRepository.get(requestId).orElseThrow(() ->
                new NotFoundException("Request [%s] is not found".formatted(requestId))
        );
        request = imageFilterRepository.update(requestId, ImageRequestStatus.DONE, editedImageId);

        if (request.editedImageId().equals(request.originalImageId())) {
            return;
        }

        long size = s3StorageService.getSize(editedImageId);


        var originalImage = imageRepository.get(request.originalImageId()).orElseThrow();
        String editedFilename = "%s (edited).%s".formatted(
                FilenameUtils.getName(originalImage.filename()),
                FilenameUtils.getExtension(originalImage.filename())
        );
        var editedImage = new ImageEntity(
                editedImageId,
                editedFilename,
                size,
                originalImage.userId()
        );
        imageRepository.save(editedImage);
    }
}
