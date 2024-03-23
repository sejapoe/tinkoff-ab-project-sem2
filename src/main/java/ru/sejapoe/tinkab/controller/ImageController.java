package ru.sejapoe.tinkab.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sejapoe.tinkab.domain.ImageEntity;
import ru.sejapoe.tinkab.dto.SuccessResponse;
import ru.sejapoe.tinkab.dto.image.ListImagesResponse;
import ru.sejapoe.tinkab.dto.image.UploadImageRequest;
import ru.sejapoe.tinkab.dto.image.UploadImageResponse;
import ru.sejapoe.tinkab.exception.NotFoundException;
import ru.sejapoe.tinkab.mapper.ImageMapper;
import ru.sejapoe.tinkab.service.ImageService;
import ru.sejapoe.tinkab.service.storage.StorageService;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    private final StorageService storageService;
    private final ImageMapper imageMapper;

    @GetMapping("/images")
    public ListImagesResponse getImages() {
        return new ListImagesResponse(imageService.getAll().stream().map(imageMapper::toImageResponse).collect(Collectors.toList()));
    }

    @GetMapping(value = "/image/{image-id}")
    public ResponseEntity<Resource> getImageById(@PathVariable("image-id") UUID imageId) {
        ImageEntity imageEntity = imageService.getById(imageId);
        Resource file = storageService.loadAsResource(imageId.toString());

        if (file == null) throw new NotFoundException("Image [%s] is not found".formatted(imageId));

        String contentDispositionHeaderValue = "attachment; filename=\"%s\"".formatted(imageEntity.filename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeaderValue)
                .body(file);
    }


    @PostMapping(value = "/image", consumes = {"multipart/form-data"})
    public UploadImageResponse postImage(@Valid @ModelAttribute UploadImageRequest request) {
        var image = imageService.saveImage(request.file());
        return new UploadImageResponse(image.id());
    }

    @DeleteMapping("/image/{image-id}")
    public SuccessResponse deleteImage(@PathVariable("image-id") UUID imageId) {
        imageService.deleteImage(imageId);
        return new SuccessResponse(true, "Image [%s] has been deleted".formatted(imageId));
    }
}
