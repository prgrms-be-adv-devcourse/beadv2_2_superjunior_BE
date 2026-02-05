package store._0982.batch.batch.grouppurchase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseWithProduct;
import store._0982.batch.batch.grouppurchase.policy.GroupPurchasePolicy;
import store._0982.batch.batch.grouppurchase.processor.OpenGroupPurchaseProcessor;
import store._0982.batch.batch.grouppurchase.writer.OpenGroupPurchaseWriter;

/**
 * 공동구매 OPEN Step 설정
 */
@Configuration
@RequiredArgsConstructor
public class OpenGroupPurchaseStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final OpenGroupPurchaseProcessor groupPurchaseOpenProcessor;
    private final OpenGroupPurchaseWriter groupPurchaseOpenWriter;

    @Bean
    public Step openGroupPurchaseStep(
            @Qualifier("openGroupPurchase") JpaPagingItemReader<GroupPurchaseWithProduct> openGroupPurchaseReader
    ) {
        return new StepBuilder("openGroupPurchaseStep", jobRepository)
                .<GroupPurchaseWithProduct, GroupPurchaseResultWithProductInfo>chunk(GroupPurchasePolicy.GroupPurchase.CHUNK_UNIT, transactionManager)
                .reader(openGroupPurchaseReader)
                .processor(groupPurchaseOpenProcessor)
                .writer(groupPurchaseOpenWriter)
                .build();
    }
}
