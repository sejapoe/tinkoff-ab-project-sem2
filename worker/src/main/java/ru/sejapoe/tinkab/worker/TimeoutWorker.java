package ru.sejapoe.tinkab.worker;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TimeoutWorker implements Worker {
    @SneakyThrows
    @Override
    public UUID doWork(UUID imageId, String filter) {
        Thread.sleep(5000);
        return imageId;
    }
}
