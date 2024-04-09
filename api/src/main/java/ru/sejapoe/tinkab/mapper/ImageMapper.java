package ru.sejapoe.tinkab.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sejapoe.tinkab.domain.ImageEntity;
import ru.sejapoe.tinkab.dto.image.ImageResponse;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "imageId", source = "id")
    ImageResponse toImageResponse(ImageEntity imageEntity);
}
