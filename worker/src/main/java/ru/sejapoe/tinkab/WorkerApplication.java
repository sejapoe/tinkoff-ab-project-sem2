package ru.sejapoe.tinkab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.sejapoe.tinkab.config.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class})
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

}
