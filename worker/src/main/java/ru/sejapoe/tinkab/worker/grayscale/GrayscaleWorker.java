package ru.sejapoe.tinkab.worker.grayscale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.worker.Worker;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "GRAYSCALE")
public class GrayscaleWorker implements Worker<Object> {
    @Override
    public BufferedImage doWork(BufferedImage bufferedImage, Object params) {
        var op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(bufferedImage, bufferedImage);
        return bufferedImage;
    }
}
