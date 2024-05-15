package ru.sejapoe.tinkab.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;
import ru.sejapoe.tinkab.exception.BaseException;
import ru.sejapoe.tinkab.exception.NotFoundException;

import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaInitializer {
    private static final Logger log = LoggerFactory.getLogger(KafkaInitializer.class);
    private final KafkaProperties kafkaProperties;

    @Value(value = "${kafka.backoff.interval}")
    private Long interval;

    @Value(value = "${kafka.backoff.max_failure}")
    private Long maxAttempts;

    @Bean
    public NewTopic imagesWipTopic() {
        return new NewTopic("images.wip", 1, (short) 3).configs(Map.of(
                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2"
        ));
    }

    @Bean
    public NewTopic imagesDoneTopic() {
        return new NewTopic("images.done", 1, (short) 3).configs(Map.of(
                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2"
        ));
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        var props = kafkaProperties.buildProducerProperties(null);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Партиция одна, так что все равно как роутить
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);

        // Отправляем сообщения сразу
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);

        // ретраи
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);


        props.put(ProducerConfig.CLIENT_ID_CONFIG, "api-producer");

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, e) -> {
            log.error(e.getMessage(), e);
        }, fixedBackOff);
        errorHandler.addNotRetryableExceptions(NotFoundException.class);
        return errorHandler;
    }
}
