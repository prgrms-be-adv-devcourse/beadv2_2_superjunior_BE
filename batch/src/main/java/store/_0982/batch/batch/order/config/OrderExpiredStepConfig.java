package store._0982.batch.batch.order.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.order.tasklet.OrderExpiredTasklet;

@Configuration
@RequiredArgsConstructor
public class OrderExpiredStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderExpiredTasklet orderExpiredTasklet;

    @Bean
    public Step orderExpiredStep(){
        return new StepBuilder("orderExpiredStep", jobRepository)
                .tasklet(orderExpiredTasklet, transactionManager)
                .build();
    }
}
