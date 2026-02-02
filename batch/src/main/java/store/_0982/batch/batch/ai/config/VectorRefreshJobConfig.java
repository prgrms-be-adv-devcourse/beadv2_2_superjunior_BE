package store._0982.batch.batch.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class VectorRefreshJobConfig {

    private static final String JOB_NAME = "vectorRefreshJob";

    private final JobRepository jobRepository;

    @Bean
    public Job vectorRefreshJob(Step vectorRefreshStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new VectorRefreshJobIncrementer())
                .start(vectorRefreshStep)
                .build();
    }
}
