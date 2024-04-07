package ru.sejapoe.tinkab.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.util.Objects;

public class PostgresTestConfig {
    private static volatile PostgreSQLContainer<?> postgreSQLContainer = null;

    private static PostgreSQLContainer<?> getPostgreSQLContainer() {
        var instance = postgreSQLContainer;
        if (Objects.isNull(instance)) {
            synchronized (PostgreSQLContainer.class) {
                instance = postgreSQLContainer;
                if (Objects.isNull(instance)) {
                    postgreSQLContainer = instance = new PostgreSQLContainer<>("postgres:15")
                            .withDatabaseName("test-database")
                            .withUsername("test-user")
                            .withPassword("test-pass")
                            .withStartupTimeout(Duration.ofSeconds(60))
                            .withReuse(true);
                    postgreSQLContainer.start();
                }
            }
        }
        return instance;
    }

    @Component("PostgresInitializer")
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var postgreSQLContainer = getPostgreSQLContainer();

            var jdbcUrl = postgreSQLContainer.getJdbcUrl();
            var username = postgreSQLContainer.getUsername();
            var password = postgreSQLContainer.getPassword();

            TestPropertyValues.of(
                    "spring.datasource.url=" + jdbcUrl,
                    "spring.datasource.username=" + username,
                    "spring.datasource.password=" + password,
                    "spring.datasource.driverClassName=" + "org.postgresql.Driver"
            ).applyTo(applicationContext.getEnvironment());

            runMigrations(postgreSQLContainer);
        }

        private void runMigrations(JdbcDatabaseContainer<?> c) {
            Flyway flyway = Flyway.configure()
                    .dataSource(c.getJdbcUrl(), c.getUsername(), c.getPassword())
                    .locations("filesystem:src/main/resources/migrations")
                    .load();

            flyway.migrate();
        }
    }
}