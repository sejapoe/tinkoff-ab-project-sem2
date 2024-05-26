package ru.sejapoe.tinkab.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sejapoe.tinkab.dto.ModifiedImageResponse;
import ru.sejapoe.tinkab.dto.SuccessResponse;
import ru.sejapoe.tinkab.dto.image.ApplyImageFilterRequest;
import ru.sejapoe.tinkab.dto.image.ApplyImageFiltersResponse;
import ru.sejapoe.tinkab.mapper.ImageFilterMapper;
import ru.sejapoe.tinkab.service.ImageFilterService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImageFilterController {
    private final ImageFilterService imageFilterService;
    private final ImageFilterMapper imageFilterMapper;

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
    @PostMapping(value = "/image/{image-id}/filters/apply", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ApplyImageFiltersResponse applyFilters(
            @PathVariable("image-id") UUID imageId,
            @RequestBody ApplyImageFilterRequest filtersRequest
    ) {
        return new ApplyImageFiltersResponse(
                imageFilterService.applyFilters(imageId, filtersRequest.filters())
        );
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
    @GetMapping(value = "/image/{image-id}/filters/{request-id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ModifiedImageResponse getRequestStatus(
            @PathVariable("image-id") UUID imageId,
            @PathVariable("request-id") UUID requestId
    ) {
        return imageFilterMapper.toModifiedImage(imageFilterService.getById(imageId, requestId));
    }
}
