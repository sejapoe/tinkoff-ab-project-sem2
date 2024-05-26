package ru.sejapoe.tinkab.worker.crop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.worker.WorkerConfig;

@Component
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "CROP")
public class
CropWorkerConfig implements WorkerConfig {

    @Override
    public Class<?> paramsClass() {
        return CropParams.class;
    }

    @Override
    public String filterName() {
        return "CROP";
    }
}
