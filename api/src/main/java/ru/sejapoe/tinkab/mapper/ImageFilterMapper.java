package ru.sejapoe.tinkab.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sejapoe.tinkab.domain.ImageFilterEntity;
import ru.sejapoe.tinkab.dto.ModifiedImageResponse;

@Mapper(componentModel = "spring")
public interface ImageFilterMapper {
    @Mapping(target = "imageId",
            expression = "java(java.util.Objects.requireNonNullElse(imageFilterEntity.editedImageId(), imageFilterEntity.originalImageId()))")
    ModifiedImageResponse toModifiedImage(ImageFilterEntity imageFilterEntity);
}
