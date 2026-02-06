package store._0982.batch.batch.settlement.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.settlement.listener.SettlementStepListener;
import store._0982.batch.batch.settlement.listener.SettlementWriterListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.writer.SettlementWriter;
import store._0982.common.domain.settlement.OrderSettlement;
import store._0982.common.exception.CustomException;

@RequiredArgsConstructor
@Configuration
public class SettlementStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final SettlementWriter settlementWriter;
    private final SettlementWriterListener settlementWriterListener;
    private final SettlementStepListener settlementStepListener;

    @Bean
    public Step settlementStep(
            JpaPagingItemReader<OrderSettlement> sellerBalanceReader) {
        return new StepBuilder("settlementStep", jobRepository)
                .<OrderSettlement, OrderSettlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(sellerBalanceReader)
                .writer(settlementWriter)
                .listener(settlementWriterListener)
                .listener(settlementStepListener)
                // 재시도 정책
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .noRetry(CustomException.class)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<OrderSettlement> sellerBalanceReader() {
        return new JpaPagingItemReaderBuilder<OrderSettlement>()
                .name("settlementReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SettlementPolicy.CHUNK_UNIT)
                .queryString("""
                        SELECT os
                        FROM OrderSettlement os
                        WHERE os.settledAt IS NULL
                        ORDER BY os.orderSettlementId
                        """)
                .build();
    }
}
