package ru.sejapoe.tinkab.worker;

import java.awt.image.BufferedImage;

public interface Worker<T> {
    BufferedImage doWork(BufferedImage bufferedImage, T params);
}
