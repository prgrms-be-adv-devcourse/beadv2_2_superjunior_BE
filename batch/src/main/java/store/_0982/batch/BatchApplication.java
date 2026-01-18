package store._0982.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
        System.exit(SpringApplication.exit(SpringApplication.run(BatchApplication.class, args)));
    }

}
