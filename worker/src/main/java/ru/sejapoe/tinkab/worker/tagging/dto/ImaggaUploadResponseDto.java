package ru.sejapoe.tinkab.worker.tagging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImaggaUploadResponseDto(
        Result result,
        ImaggaStatus status
) {
    public record Result(
            @JsonProperty("upload_id")
            String uploadId
    ) {
    }

}
