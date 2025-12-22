package store._0982.product.batch.config.step;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.product.batch.dto.GroupPurchaseResult;
import store._0982.product.batch.processor.StatusProcessor;
import store._0982.product.batch.writer.StatusWriter;
import store._0982.product.domain.grouppurchase.GroupPurchase;
import store._0982.product.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UpdateStatusStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JpaPagingItemReader<GroupPurchase> expiredGroupPurchaseReader;
    private final EntityManagerFactory entityManagerFactory;
    private final StatusProcessor statusProcessor;
    private final StatusWriter statusWriter;

    @Bean
    public Step updateStatusStep(){
        return new StepBuilder("updateStatus", jobRepository)
                .<GroupPurchase, GroupPurchaseResult>chunk(20, transactionManager)
                .reader(expiredGroupPurchaseReader)
                .processor(statusProcessor)
                .writer(statusWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<GroupPurchase> expiredGroupPurchaseReader(
            @Value("#{jobParameters['now']}") String now
    ){
        return new JpaPagingItemReaderBuilder<GroupPurchase>()
                .name("expiredGroupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT g FROM GroupPurchase g " +
                        "WHERE g.status = :status " +
                        "AND g.endDate <= :now"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.OPEN,
                        "now", OffsetDateTime.parse(now)
                ))
                .pageSize(20)
                .build();
    }
}
