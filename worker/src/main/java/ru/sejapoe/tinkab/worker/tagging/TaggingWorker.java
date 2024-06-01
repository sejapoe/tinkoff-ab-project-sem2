package ru.sejapoe.tinkab.worker.tagging;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.utils.ImageUtils;
import ru.sejapoe.tinkab.worker.Worker;

import java.awt.*;
import java.awt.image.BufferedImage;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "TAG")
public class TaggingWorker implements Worker<Object> {
    private static final int FONT_SIZE = 32;
    private static final String FONT = "Arial";

    private final ImaggaClient imaggaClient;

    @Override
    public BufferedImage doWork(BufferedImage bufferedImage, Object params) {
        var imageResource = ImageUtils.bufferedImageToResource(bufferedImage);
        var uploadId = imaggaClient.postImage(imageResource).result().uploadId();
        var tags = imaggaClient.getTags(uploadId).result().tags();

        var baseFont = new Font(FONT, Font.PLAIN, FONT_SIZE);
        var graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.WHITE);
        var maxTagLength = tags.stream().mapToInt(value -> value.tag().en().length()).max().orElse(0);
        graphics.fillRect(10, 10, maxTagLength * FONT_SIZE / 2 + 20, tags.size() * FONT_SIZE + 20);

        graphics.setColor(Color.BLACK);
        var max = 0;
        for (var i = 0; i < tags.size(); i++) {
            var tag = tags.get(i).tag().en();
            max = Math.max(max, tag.length());
            graphics.setFont(baseFont.deriveFont((float) FONT_SIZE));
            graphics.drawString(tag, 15, 15 + (i + 1) * FONT_SIZE);
        }
        return bufferedImage;
    }
}
