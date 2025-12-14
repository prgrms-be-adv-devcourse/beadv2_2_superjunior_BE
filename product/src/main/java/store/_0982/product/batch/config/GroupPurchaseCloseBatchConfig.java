package store._0982.product.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GroupPurchaseCloseBatchConfig {
    private final JobRepository jobRepository;

    @Bean
    public Job closeExpiredGroupPurchaseJob(
            Step updateStatusStep,
            Step returnStep
    ){
        return new JobBuilder("closeExpiredGroupPurchase", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateStatusStep)
                .next(returnStep)
                .build();
    }
}
