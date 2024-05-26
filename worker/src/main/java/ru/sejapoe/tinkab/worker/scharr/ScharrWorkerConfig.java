package ru.sejapoe.tinkab.worker.scharr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.worker.WorkerConfig;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "SCHARR")
public class ScharrWorkerConfig implements WorkerConfig {
    @Override
    public Class<?> paramsClass() {
        return null;
    }

    @Override
    public String filterName() {
        return "SCHARR";
    }
}
