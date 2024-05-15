package ru.sejapoe.tinkab.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import ru.sejapoe.tinkab.kafka.message.ImageDoneMessage;
import ru.sejapoe.tinkab.kafka.message.ImageWipMessage;
import ru.sejapoe.tinkab.worker.Worker;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final Worker worker;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = "images.wip",
            groupId = "worker",
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
    public void consume(ConsumerRecord<String, ImageWipMessage> record, Acknowledgment acknowledgment) {
        log.info("""
                Получено следующее сообщение из топика {}:
                key: {},
                value: {}
                """, record.topic(), record.key(), record.value());
        ImageWipMessage imageWipMessage = record.value();
        if (imageWipMessage.filters().isEmpty()) {
            log.error("Empty WIP filters");
            imageWipMessage.filters().add("PASS");
        }
        String filter = imageWipMessage.filters().remove(0);
        UUID newImageId = worker.doWork(imageWipMessage.imageId(), filter);

        ProducerRecord<String, Object> producerRecord;

        if (imageWipMessage.filters().isEmpty()) { // is done
            producerRecord = new ProducerRecord<>(
                    "images.done",
                    new ImageDoneMessage(newImageId, imageWipMessage.requestId())
            );
        } else {
            producerRecord = new ProducerRecord<>(
                    "images.wip",
                    imageWipMessage
            );
        }

        kafkaTemplate.send(producerRecord).join();
        acknowledgment.acknowledge();

//        kafkaTemplate.send(producerRecord).handle((stringObjectSendResult, throwable) -> {
//            if (throwable == null) {
//                acknowledgment.acknowledge();
//            } else {
//                log.error("Failed to send message to kafka", throwable);
//            }
//            return 0;
//        });
    }
}