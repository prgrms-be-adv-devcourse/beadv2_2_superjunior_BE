package store._0982.batch.batch.settlement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.batch.settlement.listener.SettlementJobListener;

@RequiredArgsConstructor
@Configuration
public class SettlementJobConfig {

    private final JobRepository jobRepository;
    private final Step settlementStep;
    private final SettlementJobListener settlementJobListener;

    @Bean
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
                .incrementer(new DailyIncrementer())
                .start(settlementStep)
                .listener(settlementJobListener)
                .build();
    }
}
