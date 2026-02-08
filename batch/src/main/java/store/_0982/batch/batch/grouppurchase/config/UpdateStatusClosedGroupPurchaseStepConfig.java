package store._0982.batch.batch.grouppurchase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.batch.grouppurchase.policy.GroupPurchasePolicy;
import store._0982.batch.batch.grouppurchase.processor.UpdateStatusClosedGroupPurchaseProcessor;
import store._0982.batch.batch.grouppurchase.writer.UpdateStatusClosedGroupPurchaseWriter;

@Configuration
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UpdateStatusClosedGroupPurchaseProcessor updateStatusClosedGroupPurchaseProcessor;
    private final UpdateStatusClosedGroupPurchaseWriter updateStatusClosedGroupPurchaseWriter;

    @Bean
    public Step updateStatusClosedGroupPurchaseStep(
            @Qualifier("updateStatusClosedGroupPurchase") JpaCursorItemReader<GroupPurchaseProjection> updateStatusClosedGroupPurchaseReader
    ) {
        return new StepBuilder("updateStatusClosedGroupPurchaseStep", jobRepository)
                .<GroupPurchaseProjection, GroupPurchaseResultProjection>chunk(GroupPurchasePolicy.GroupPurchase.CHUNK_UNIT, transactionManager)
                .reader(updateStatusClosedGroupPurchaseReader)
                .processor(updateStatusClosedGroupPurchaseProcessor)
                .writer(updateStatusClosedGroupPurchaseWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(OptimisticLockingFailureException.class)
                .build();
    }
}
