package store._0982.batch.batch.order.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OrderExpiredJobConfig {

    private final JobRepository jobRepository;
    private final Step orderExpiredStep;

    @Bean
    public Job orderExpiredJob() {
        return new JobBuilder("orderExpiredJob", jobRepository)
                .incrementer(new OrderExpiredJobIncrementer())
                .start(orderExpiredStep)
                .build();
    }
}
