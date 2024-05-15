package ru.sejapoe.tinkab.kafka.message;

import java.util.UUID;

public record ImageDoneMessage(
        UUID imageId,
        UUID requestId
) {
}
