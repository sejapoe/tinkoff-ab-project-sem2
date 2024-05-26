package ru.sejapoe.tinkab.worker.laplacian;

import ij.ImagePlus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.worker.Worker;

import java.awt.image.BufferedImage;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "LAPLACIAN")
public class LaplacianWorker implements Worker<Object> {

    public static final int[] KERNEL = {0, 1, 0,
            1, -4, 1,
            0, 1, 0};

    @Override
    public BufferedImage doWork(BufferedImage bufferedImage, Object params) {
        var processor = new ImagePlus("Image", bufferedImage).getProcessor();
        processor.convolve3x3(KERNEL);
        return processor.getBufferedImage();
    }
}
