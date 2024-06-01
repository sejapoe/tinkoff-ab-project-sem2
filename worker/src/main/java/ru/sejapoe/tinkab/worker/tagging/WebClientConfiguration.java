package ru.sejapoe.tinkab.worker.tagging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Configuration
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "TAG")
public class WebClientConfiguration {
    private static final int TIMEOUT = 5000;
    private static final String IMAGGA_URL = "https://api.imagga.com/v2";
    @Value("${imagga.key}")
    private String key;
    @Value("${imagga.secret}")
    private String secret;

    @Bean
    public WebClient restClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(TIMEOUT));

        return WebClient.builder()
                .baseUrl(IMAGGA_URL)
                .filter(basicAuthentication(key, secret))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}