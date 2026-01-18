package store._0982.batch.batch.sellerbalance.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.batch.settlement.config.TimestampIncrementer;

@RequiredArgsConstructor
@Configuration
public class SellerBalanceJobConfig {

    private final JobRepository jobRepository;
    private final Step sellerBalanceStep;

    @Bean
    public Job sellerBalanceJob() {
        return new JobBuilder("sellerBalanceJob", jobRepository)
                .incrementer(new TimestampIncrementer())
                .start(sellerBalanceStep)
                .build();
    }
}
