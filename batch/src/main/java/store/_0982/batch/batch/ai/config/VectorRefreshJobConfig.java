package store._0982.batch.batch.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.application.commerce.CommerceQueryPort;
import store._0982.batch.domain.ai.*;

import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class VectorRefreshJobConfig {

    private static final String JOB_NAME = "vectorRefreshJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CommerceQueryPort commerceQueryPort;
    private final PersonalVectorRepository personalVectorRepository;

    @Bean
    public Job vectorRefreshJob(Step vectorRefreshStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(vectorRefreshStep)
                .build();
    }

    @Bean
    public Step vectorRefreshStep(
            ItemReader<MemberVectorsInput> vectorRefreshReader,
            ItemProcessor<MemberVectorsInput, PersonalVector> vectorRefreshProcessor,
            ItemWriter<PersonalVector> vectorRefreshWriter
    ) {
        return new StepBuilder("vectorRefreshStep", jobRepository)
                .<MemberVectorsInput, PersonalVector>chunk(1, transactionManager) // process and write each item individually
                .reader(vectorRefreshReader)
                .processor(vectorRefreshProcessor)
                .writer(vectorRefreshWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<MemberVectorsInput> vectorRefreshReader(
            @Value("#{jobParameters['memberId']}") String memberId
    ) {
        return new SingleMemberVectorsReader(commerceQueryPort, memberId);
    }

    @Bean
    public ItemProcessor<MemberVectorsInput, PersonalVector> vectorRefreshProcessor() {
        return item ->
                PersonalVector.create(item.memberId(), VectorUtil.getAverageVector(item.cartVectors(), item.orderVectors()));
    }

    @Bean
    public ItemWriter<PersonalVector> vectorRefreshWriter() {
        return personalVectorRepository::saveAll; //Id가 겹치면 자동으로 update, 영속성 컨텍스트에 있든 없든 상관 X
    }

    private static class SingleMemberVectorsReader implements ItemReader<MemberVectorsInput> {
        private final CommerceQueryPort commerceQueryPort;
        private final UUID memberId;
        private boolean consumed = false;

        private SingleMemberVectorsReader(CommerceQueryPort commerceQueryPort, String memberId) {
            this.commerceQueryPort = commerceQueryPort;
            if (memberId == null || memberId.isBlank()) {
                throw new IllegalArgumentException("memberId job parameter is required for vectorRefreshJob");
            }
            this.memberId = UUID.fromString(memberId);
        }

        @Override
        public MemberVectorsInput read() {
            if (consumed) {
                return null;
            }

            List<CartVector> cartVectors = commerceQueryPort.getCarts(memberId);
            List<OrderVector> orderVectors = commerceQueryPort.getOrders(memberId);
            consumed = true;
            return new MemberVectorsInput(memberId, cartVectors, orderVectors);
        }
    }

    private record MemberVectorsInput(
            UUID memberId,
            List<CartVector> cartVectors,
            List<OrderVector> orderVectors
    ) {
    }
}
