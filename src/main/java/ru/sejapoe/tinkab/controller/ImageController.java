package ru.sejapoe.tinkab.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sejapoe.tinkab.domain.ImageEntity;
import ru.sejapoe.tinkab.dto.SuccessResponse;
import ru.sejapoe.tinkab.dto.image.ListImagesResponse;
import ru.sejapoe.tinkab.dto.image.UploadImageResponse;
import ru.sejapoe.tinkab.exception.NotFoundException;
import ru.sejapoe.tinkab.mapper.ImageMapper;
import ru.sejapoe.tinkab.service.ImageService;
import ru.sejapoe.tinkab.service.storage.StorageService;
import ru.sejapoe.tinkab.validation.annotations.MaxSize;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    private final StorageService storageService;
    private final ImageMapper imageMapper;

    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),
    })
    @GetMapping(value = "/images", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ListImagesResponse getImages() {
        return new ListImagesResponse(imageService.getAll()
                .stream()
                .map(imageMapper::toImageResponse)
                .collect(Collectors.toList()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),

    })
    @GetMapping(value = "/image/{image-id}")
    public ResponseEntity<Resource> getImageById(@PathVariable("image-id") UUID imageId) {
        ImageEntity imageEntity = imageService.getById(imageId);
        Resource file = storageService.loadAsResource(imageId.toString());

        if (file == null) {
            throw new NotFoundException("Image [%s] is not found".formatted(imageId));
        }

        String contentDispositionHeaderValue = "attachment; filename=\"%s\"".formatted(imageEntity.filename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionHeaderValue)
                .body(file);
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),
            @ApiResponse(responseCode = "400",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),
    })
    @PostMapping(value = "/image",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public UploadImageResponse postImage(
            @RequestParam("file")
            @NotNull
            @MaxSize("10M")
            @ru.sejapoe.tinkab.validation.annotations.MediaType("image/jpeg,image/png")
            MultipartFile file
    ) {
        var image = imageService.saveImage(file);
        return new UploadImageResponse(image.id());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "500",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),
            @ApiResponse(responseCode = "404",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class)
                    )),

    })
    @DeleteMapping(value = "/image/{image-id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public SuccessResponse deleteImage(@PathVariable("image-id") UUID imageId) {
        imageService.deleteImage(imageId);
        return new SuccessResponse(true, "Image [%s] has been deleted".formatted(imageId));
    }
}
