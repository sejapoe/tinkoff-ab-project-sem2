package ru.sejapoe.tinkab.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.kafka.message.ImageDoneMessage;
import ru.sejapoe.tinkab.service.ImageFilterService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ImageFilterService imageFilterService;

    @KafkaListener(
            topics = "images.done",
            groupId = "api.done",
            properties = {
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
                    ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
                    ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG + "=org.apache.kafka.clients.consumer.RoundRobinAssignor",
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG + "=org.apache.kafka.common.serialization.StringDeserializer",
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + "=org.springframework.kafka.support.serializer.JsonDeserializer",
                    JsonDeserializer.TRUSTED_PACKAGES + "=ru.sejapoe.tinkab.kafka.message",
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG + "=earliest"
            }
    )
    public void consume(ConsumerRecord<String, ImageDoneMessage> record, Acknowledgment acknowledgment) {
        log.info("""
                Получено следующее сообщение из топика {}:
                key: {},
                value: {}
                """, record.topic(), record.key(), record.value());
        try {
            imageFilterService.setDone(record.value().requestId(), record.value().imageId());
            acknowledgment.acknowledge();
            log.info("Successfully handled message {}", record.value().imageId());
        } catch (Exception e) {
            log.error("Failed to consume kafka message", e);
        }
    }
}
