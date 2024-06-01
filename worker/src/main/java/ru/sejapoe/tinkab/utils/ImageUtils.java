package ru.sejapoe.tinkab.utils;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@UtilityClass
public class ImageUtils {
    public static Resource bufferedImageToResource(BufferedImage bufferedImage) {
        var outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayResource(outputStream.toByteArray()) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        };
    }
}
