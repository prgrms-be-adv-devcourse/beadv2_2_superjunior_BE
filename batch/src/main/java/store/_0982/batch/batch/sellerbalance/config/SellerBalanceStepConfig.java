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
import store._0982.batch.batch.sellerbalance.policy.SellerBalancePolicy;
import store._0982.batch.batch.sellerbalance.processor.SellerBalanceProcessor;
import store._0982.batch.batch.sellerbalance.writer.SellerBalanceWriter;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SellerBalanceStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final SellerBalanceProcessor sellerBalanceProcessor;
    private final SellerBalanceWriter sellerBalanceWriter;

    @Bean
    public Step sellerBalanceStep(
            JpaPagingItemReader<GroupPurchase> sellerBalanceReader) {
        return new StepBuilder("sellerBalanceStep", jobRepository)
                .<GroupPurchase, GroupPurchase>chunk(SellerBalancePolicy.CHUNK_UNIT, transactionManager)
                .reader(sellerBalanceReader)
                .processor(sellerBalanceProcessor)
                .writer(sellerBalanceWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<GroupPurchase> sellerBalanceReader() {
        return new JpaPagingItemReaderBuilder<GroupPurchase>()
                .name("sellerBalanceReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SellerBalancePolicy.CHUNK_UNIT)
                .queryString("""
                        SELECT gp
                        FROM GroupPurchase gp
                        WHERE gp.status = 'SUCCESS'
                        AND gp.endDate <= :twoWeeksAgo
                        AND gp.settledAt IS NULL
                        """)
                .parameterValues(Map.of(
                        "twoWeeksAgo", SellerBalancePolicy.getTwoWeeksAgo()
                ))
                .build();
    }
}
