package ru.sejapoe.tinkab.domain;

import java.util.UUID;

public record WorkerLogEntity(
        UUID imageId,
        UUID requestId,
        String filterType
) {
}
