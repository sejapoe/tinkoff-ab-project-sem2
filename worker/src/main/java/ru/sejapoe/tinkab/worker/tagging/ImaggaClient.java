package ru.sejapoe.tinkab.worker.tagging;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.sejapoe.tinkab.worker.tagging.dto.ImaggaTagsResponseDto;
import ru.sejapoe.tinkab.worker.tagging.dto.ImaggaUploadResponseDto;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "filter", name = "type", havingValue = "TAG")
public class ImaggaClient {
    private final WebClient webClient;

    @Retry(name = "imaggaRetry")
    @CircuitBreaker(name = "imaggaCircuitBreaker")
    @RateLimiter(name = "imaggaRateLimiter")
    public ImaggaUploadResponseDto postImage(Resource resource) {
        return webClient
                .post()
                .uri("/uploads")
                .body(BodyInserters.fromMultipartData("image", resource))
                .retrieve()
                .bodyToMono(ImaggaUploadResponseDto.class)
                .doOnSuccess(imaggaUploadResponseDto -> log.info("Got: {}", imaggaUploadResponseDto))
                .block();
    }

    @Retry(name = "imaggaRetry")
    @CircuitBreaker(name = "imaggaCircuitBreaker")
    @RateLimiter(name = "imaggaRateLimiter")
    public ImaggaTagsResponseDto getTags(String uploadId) {
        return webClient
                .get()
                .uri("/tags?limit=3&image_upload_id=" + uploadId)
                .retrieve()
                .bodyToMono(ImaggaTagsResponseDto.class)
                .doOnError(exception -> {
                    log.error("Failed cause {}", exception.getMessage());
                    if (exception instanceof WebClientResponseException) {
                        log.error("Body: {}", ((WebClientResponseException) exception).getResponseBodyAsString());
                    }
                })
                .block();
    }
}
