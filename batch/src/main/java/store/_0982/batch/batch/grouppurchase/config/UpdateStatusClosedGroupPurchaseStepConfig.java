package store._0982.batch.batch.grouppurchase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResult;
import store._0982.batch.batch.grouppurchase.policy.GroupPurchasePolicy;
import store._0982.batch.batch.grouppurchase.processor.UpdateStatusClosedGroupPurchaseProcessor;
import store._0982.batch.batch.grouppurchase.reader.UpdateStatusClosedGroupPurchaseReader;
import store._0982.batch.batch.grouppurchase.writer.UpdateStatusClosedGroupPurchaseWriter;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

@Configuration
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UpdateStatusClosedGroupPurchaseReader updateStatusClosedGroupPurchaseReader;
    private final UpdateStatusClosedGroupPurchaseProcessor updateStatusClosedGroupPurchaseProcessor;
    private final UpdateStatusClosedGroupPurchaseWriter updateStatusClosedGroupPurchaseWriter;

    @Bean
    public Step updateStatusClosedGroupPurchaseStep(){
        return new StepBuilder("updateStatusClosedGroupPurchaseStep", jobRepository)
                .<GroupPurchase, GroupPurchaseResult>chunk(GroupPurchasePolicy.GroupPurchase.CHUNK_UNIT, transactionManager)
                .reader(updateStatusClosedGroupPurchaseReader.create())
                .processor(updateStatusClosedGroupPurchaseProcessor)
                .writer(updateStatusClosedGroupPurchaseWriter)
                .build();
    }

}
