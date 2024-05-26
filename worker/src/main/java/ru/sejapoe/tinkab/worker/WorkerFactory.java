package ru.sejapoe.tinkab.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.domain.WorkerLogEntity;
import ru.sejapoe.tinkab.exception.AlreadyHandledException;
import ru.sejapoe.tinkab.exception.UnsupportedFilterException;
import ru.sejapoe.tinkab.repo.WorkerLogRepository;
import ru.sejapoe.tinkab.service.storage.StorageService;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkerFactory {
    private static final Logger log = LoggerFactory.getLogger(WorkerFactory.class);
    private final WorkerConfig workerConfig;
    private final BeanFactory beanFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StorageService storageService;

    private final Worker<?> worker;
    private final WorkerLogRepository workerLogRepository;

    public UUID doWork(UUID imageId, UUID requestId, String filterName, Object filterParams, boolean isTemp) {
        if (!workerConfig.filterName().equals(filterName)) {
            throw new UnsupportedFilterException();
        }

        var workerLog = new WorkerLogEntity(imageId, requestId, filterName);
        if (workerLogRepository.existsById(workerLog)) {
            throw new AlreadyHandledException();
        }

        var contentType = storageService.getContentType(imageId);
        var bytes = storageService.loadAsBytes(imageId);

        var bufferedImage = read(bytes);
        BufferedImage newImage = doWork(bufferedImage, filterParams);

        var newBytes = write(contentType, newImage);
        var storedUuid = storageService.store(newBytes, contentType, isTemp);

        workerLogRepository.save(workerLog);

        return storedUuid;
    }

    @SneakyThrows
    private static BufferedImage read(byte[] bytes) {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    @SneakyThrows
    private static byte[] write(String contentType, BufferedImage newImage) {
        ImageWriter imageWriter = ImageIO.getImageWritersByMIMEType(contentType).next();

        var os = new ByteArrayOutputStream();
        var imageOs = ImageIO.createImageOutputStream(os);
        imageWriter.setOutput(imageOs);
        imageWriter.write(newImage);

        return os.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private BufferedImage doWork(BufferedImage bytes, Object filterParams) {
        if (filterParams == null) {
            return worker.doWork(bytes, null);
        } else {
            Object params = objectMapper.convertValue(filterParams, workerConfig.paramsClass());
            return ((Worker<Object>) worker).doWork(bytes, params);
        }
    }
}
