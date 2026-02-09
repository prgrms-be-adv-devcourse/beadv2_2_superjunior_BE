package store._0982.batch.batch.recommendation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.recommendation.processor.PersonalVectorProcessor;
import store._0982.batch.batch.recommendation.reader.PersonalVectorInfoReader;
import store._0982.batch.batch.recommendation.reader.PersonalVectorInfoReader.MemberVectorsInput;
import store._0982.batch.batch.recommendation.writer.PersonalVectorWriter;
import store._0982.batch.domain.recommendation.PersonalVector;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class VectorRefreshStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final PersonalVectorInfoReader personalVectorInfoReader;
    private final PersonalVectorProcessor personalVectorProcessor;
    private final PersonalVectorWriter personalVectorWriter;

    @Bean
    public Step vectorRefreshStep() {
        return new StepBuilder("vectorRefreshStep", jobRepository)
                .<List<MemberVectorsInput>, List<PersonalVector>>chunk(1, transactionManager) // process and write each item individually
                .reader(personalVectorInfoReader)
                .processor(personalVectorProcessor)
                .writer(personalVectorWriter)
                .build();
    }
}
