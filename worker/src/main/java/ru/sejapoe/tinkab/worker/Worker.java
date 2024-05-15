package ru.sejapoe.tinkab.worker;

import java.util.UUID;

public interface Worker {
    /**
     * @param imageId - id of given image in storage
     * @param filter  filter to apply for image
     * @return id of edited image
     */
    UUID doWork(UUID imageId, String filter);
}
