package store._0982.batch.batch.sellerbalance.config;

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
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.sellerbalance.listener.SellerBalanceStepListener;
import store._0982.batch.batch.sellerbalance.listener.SellerBalanceWriterListener;
import store._0982.batch.batch.sellerbalance.policy.SellerBalancePolicy;
import store._0982.batch.batch.sellerbalance.writer.SellerBalanceWriter;
import store._0982.batch.domain.settlement.OrderSettlement;

@RequiredArgsConstructor
@Configuration
public class SellerBalanceStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final SellerBalanceWriter sellerBalanceWriter;
    private final SellerBalanceWriterListener sellerBalanceWriterListener;
    private final SellerBalanceStepListener sellerBalanceStepListener;

    @Bean
    public Step sellerBalanceStep(
            JpaPagingItemReader<OrderSettlement> sellerBalanceReader) {
        return new StepBuilder("sellerBalanceStep", jobRepository)
                .<OrderSettlement, OrderSettlement>chunk(SellerBalancePolicy.CHUNK_UNIT, transactionManager)
                .reader(sellerBalanceReader)
                .writer(sellerBalanceWriter)
                .listener(sellerBalanceWriterListener)
                .listener(sellerBalanceStepListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<OrderSettlement> sellerBalanceReader() {
        return new JpaPagingItemReaderBuilder<OrderSettlement>()
                .name("sellerBalanceReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SellerBalancePolicy.CHUNK_UNIT)
                .queryString("""
                        SELECT os
                        FROM OrderSettlement os
                        WHERE os.settledAt IS NULL
                        ORDER BY os.orderSettlementId
                        """)
                .build();
    }
}
