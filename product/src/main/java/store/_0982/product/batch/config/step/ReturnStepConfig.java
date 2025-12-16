package store._0982.product.batch.config.step;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.product.batch.dto.GroupPurchaseResult;
import store._0982.product.batch.processor.ReturnProcessor;
import store._0982.product.batch.writer.ReturnWriter;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseStatus;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ReturnStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final ReturnProcessor returnProcessor;
    private final ReturnWriter returnWriter;

    @Bean
    public Step returnStep(){
        return new StepBuilder("returnStep", jobRepository)
                .<GroupPurchase, GroupPurchaseResult>chunk(50, transactionManager)
                .reader(failedGroupPurchaseReader())
                .processor(returnProcessor)
                .writer(returnWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<GroupPurchase> failedGroupPurchaseReader(){
        return new JpaPagingItemReaderBuilder<GroupPurchase>()
                .name("failedGroupPurchaseReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT g FROM GroupPurchase g " +
                        "WHERE g.status = :status " +
                        "AND g.returnedAt IS NULL"
                )
                .parameterValues(Map.of(
                        "status", GroupPurchaseStatus.FAILED
                ))
                .pageSize(50)
                .build();
    }
}
