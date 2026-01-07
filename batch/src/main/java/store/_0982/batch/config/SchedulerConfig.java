package store._0982.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("local")
@EnableScheduling
@Configuration
public class SchedulerConfig {
}
