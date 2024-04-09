package ru.sejapoe.tinkab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.sejapoe.tinkab.config.JwtProperties;
import ru.sejapoe.tinkab.config.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, JwtProperties.class})
public class TinkAbApplication {

    public static void main(String[] args) {
        SpringApplication.run(TinkAbApplication.class, args);
    }

}
