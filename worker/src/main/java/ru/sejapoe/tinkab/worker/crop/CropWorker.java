package ru.sejapoe.tinkab.worker.crop;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.worker.Worker;

import java.awt.image.BufferedImage;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "CROP")
public class CropWorker implements Worker<CropParams> {
    @SneakyThrows
    public BufferedImage doWork(BufferedImage bufferedImage, CropParams cropParams) {
        log.info(cropParams.toString());
        return bufferedImage.getSubimage(cropParams.rect()[0],
                cropParams.rect()[1],
                cropParams.rect()[2] - cropParams.rect()[0],
                cropParams.rect()[3] - cropParams.rect()[1]);
    }
}
