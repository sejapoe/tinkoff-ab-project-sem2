package ru.sejapoe.tinkab.kafka.message;

import java.util.List;
import java.util.UUID;

public record ImageWipMessage(
        UUID imageId,
        UUID requestId,
        List<String> filters
) {
}
