package ru.sejapoe.tinkab.worker.scharr;

import ij.ImagePlus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.worker.Worker;

import java.awt.image.BufferedImage;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "SCHARR")
public class ScharrWorker implements Worker<Object> {

    public static final int[] KERNEL = {-3, -10, -3,
            0, 0, 0,
            3, 10, 3};

    @Override
    public BufferedImage doWork(BufferedImage bufferedImage, Object params) {
        var processor = new ImagePlus("Image", bufferedImage).getProcessor();
        processor.convolve3x3(KERNEL);
        return processor.getBufferedImage();
    }
}
