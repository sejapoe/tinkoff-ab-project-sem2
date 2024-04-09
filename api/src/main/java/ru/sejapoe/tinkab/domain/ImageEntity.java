package ru.sejapoe.tinkab.domain;

import java.util.UUID;

/**
 * @param id       UUID of object stored in storage
 * @param filename Original image name
 * @param size     Size in bytes
 */
public record ImageEntity(UUID id, String filename, Long size, Long userId) {
}
