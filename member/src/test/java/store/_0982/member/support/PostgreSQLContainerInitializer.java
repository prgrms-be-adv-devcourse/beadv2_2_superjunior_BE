package store._0982.member.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@SuppressWarnings({"resource", "SpellCheckingInspection"})
public class PostgreSQLContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>("pgvector/pgvector:pg15")
                    .withDatabaseName("member_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRESQL.start();
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext context) {
        Map<String, String> properties = Map.of(
                "spring.datasource.url", POSTGRESQL.getJdbcUrl(),
                "spring.datasource.username", POSTGRESQL.getUsername(),
                "spring.datasource.password", POSTGRESQL.getPassword()
        );
        TestPropertyValues.of(properties).applyTo(context);
    }
}
