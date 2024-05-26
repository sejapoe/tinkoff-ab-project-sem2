package ru.sejapoe.tinkab.service.storage;

import java.util.UUID;

/**
 * The StorageService interface provides methods for initializing, storing, loading, and deleting files from a storage.
 */
public interface StorageService {
    void init();

    UUID store(byte[] bytes, String contentType, boolean isTemp);

    byte[] loadAsBytes(UUID filename);

    String getContentType(UUID uuid);
}