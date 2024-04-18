package ru.sejapoe.tinkab.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.stream.Stream;

public class KafkaTestConfig {
    private static final KafkaContainer kafka1Container;
    private static final KafkaContainer kafka2Container;
    private static final KafkaContainer kafka3Container;
    private static final GenericContainer<?> zookeeperContainer;
    private static final Logger log = LoggerFactory.getLogger(KafkaTestConfig.class);

    static {
        var network = Network.newNetwork();

        zookeeperContainer = initZookeeperContainer(network);
        kafka1Container = initKafkaBrokerContainer(1, network);
        kafka2Container = initKafkaBrokerContainer(2, network);
        kafka3Container = initKafkaBrokerContainer(3, network);
        Stream.of(zookeeperContainer, kafka1Container, kafka2Container, kafka3Container).parallel().forEach(Startable::start);
    }

    private static KafkaContainer initKafkaBrokerContainer(int brokerId, Network network) {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withNetwork(network)
                .withNetworkAliases("kafka-" + brokerId)
                .withEnv("KAFKA_BROKER_ID", Integer.toString(brokerId))
                .withExternalZookeeper("zookeeper:2181")
                .dependsOn(zookeeperContainer)
                .withStartupTimeout(Duration.ofSeconds(60))
                .withReuse(true);
    }

    private static GenericContainer<?> initZookeeperContainer(Network network) {
        return new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:latest"))
                .withNetwork(network)
                .withNetworkAliases("zookeeper")
                .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
                .withStartupTimeout(Duration.ofSeconds(60))
                .withReuse(true);
    }

    @Component("KafkaInitializer")
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            String boostrapServers = kafka1Container.getBootstrapServers() + "," + kafka2Container.getBootstrapServers() + "," + kafka3Container.getBootstrapServers();
            log.info(boostrapServers);
            TestPropertyValues.of(
                    "spring.kafka.bootstrap-servers=" + boostrapServers
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
