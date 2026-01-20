package store._0982.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Profiles;

import javax.swing.*;

/**
 * Batch Application for Kubernetes CronJob execution.
 * <p>
 * Scheduling is managed by Kubernetes CronJob, not Spring Scheduler.
 * <p>
 * For local development, enable @EnableScheduling
 * and activate scheduler configurations via Spring Profile.
 */
@EnableFeignClients
@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BatchApplication.class, args);

        if (!context.getEnvironment().acceptsProfiles(Profiles.of("dev"))) {
            System.exit(SpringApplication.exit(context));
        }
    }
}
