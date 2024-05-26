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
import ru.sejapoe.tinkab.exception.AlreadyHandledException;
import ru.sejapoe.tinkab.exception.UnsupportedFilterException;
import ru.sejapoe.tinkab.kafka.message.ImageDoneMessage;
import ru.sejapoe.tinkab.kafka.message.ImageWipMessage;
import ru.sejapoe.tinkab.worker.WorkerFactory;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WorkerFactory workerFactory;

    @KafkaListener(
            topics = "images.wip",
            groupId = "${filter.kafka.group-id}",
            properties = {
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG + "=false",
                    ConsumerConfig.ISOLATION_LEVEL_CONFIG + "=read_committed",
                    ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG + "=org.apache.kafka.clients.consumer.RoundRobinAssignor",
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG + "=org.apache.kafka.common.serialization.StringDeserializer",
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG + "=org.springframework.kafka.support.serializer.JsonDeserializer",
                    JsonDeserializer.TRUSTED_PACKAGES + "=ru.sejapoe.tinkab.kafka.message",
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG + "=earliest",
            }
    )
    public void consume(ConsumerRecord<String, ImageWipMessage> record, Acknowledgment acknowledgment) {
        log.info("""
                Получено следующее сообщение из топика {}:
                key: {},
                value: {}
                """, record.topic(), record.key(), record.value());
        ImageWipMessage imageWipMessage = record.value();

        ProducerRecord<String, Object> producerRecord = produce(imageWipMessage);

        if (producerRecord != null) {
            kafkaTemplate.send(producerRecord).join();
        }

        acknowledgment.acknowledge();
    }

    private ProducerRecord<String, Object> produce(ImageWipMessage imageWipMessage) {
        if (imageWipMessage.filters().isEmpty()) {
            log.error("Empty WIP filters");
            return new ProducerRecord<>(
                    "images.done",
                    new ImageDoneMessage(imageWipMessage.imageId(), imageWipMessage.requestId())
            );
        }

        ImageWipMessage.Filter filter = imageWipMessage.filters().remove(0);

        boolean isTemp = !imageWipMessage.filters().isEmpty();

        UUID newImageId;
        try {
            newImageId = workerFactory.doWork(imageWipMessage.imageId(), imageWipMessage.requestId(), filter.type(), filter.params(), isTemp);
        } catch (UnsupportedFilterException e) {
            log.info("Unsupported filter");
            return null;
        } catch (AlreadyHandledException e) {
            log.info("Already handled");
            return null;
        } catch (Exception e) {
            log.error("unexpected error during applying filter to image", e);
            return null;
        }

        if (isTemp) {
            return new ProducerRecord<>(
                    "images.wip",
                    new ImageWipMessage(newImageId, imageWipMessage.requestId(), imageWipMessage.filters())
            );
        } else { // is done
            return new ProducerRecord<>(
                    "images.done",
                    new ImageDoneMessage(newImageId, imageWipMessage.requestId())
            );
        }
    }
}