package ru.sejapoe.tinkab.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * The StorageService interface provides methods for initializing, storing, loading, and deleting files from a storage.
 */
public interface StorageService {
    /**
     * Initializes the storage service.
     */
    void init();

    /**
     * Stores the given file in the storage.
     *
     * @param file the file to be stored
     * @return returns stored file id
     */
    UUID store(MultipartFile file);

    /**
     * Remove the given file from the storage.
     *
     * @param uuid the file to be stored
     */
    void remove(UUID uuid);

    /**
     * Loads the file with the specified filename from the storage and returns it as a Resource object.
     *
     * @param filename the name of the file to be loaded
     * @return the loaded file as a Resource object
     */
    Resource loadAsResource(String filename);
}