package store._0982.batch.batch.elasticsearch.config.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.elasticsearch.config.GroupPurchaseReindexProperties;
import store._0982.batch.batch.elasticsearch.processor.GroupPurchaseReindexProcessor;
import store._0982.batch.batch.elasticsearch.reader.GroupPurchaseReindexReader;
import store._0982.batch.batch.elasticsearch.writer.GroupPurchaseReindexWriter;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRepository;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRow;
import store._0982.batch.domain.elasticsearch.GroupPurchaseDocument;
import store._0982.batch.application.elasticsearch.GroupPurchaseReindexService;

@Configuration
@RequiredArgsConstructor
public class GroupPurchaseIncrementalReindexStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GroupPurchaseReindexProperties properties;
    private final GroupPurchaseReindexRepository reindexRepository;
    private final GroupPurchaseReindexService reindexService;

    @Bean
    public Step groupPurchaseIncrementalReindexStep(
            @Qualifier("groupPurchaseIncrementalReindexReader") ItemReader<GroupPurchaseReindexRow> groupPurchaseIncrementalReindexReader,
            @Qualifier("groupPurchaseIncrementalReindexProcessor") ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseDocument> groupPurchaseIncrementalReindexProcessor,
            @Qualifier("groupPurchaseIncrementalReindexWriter") ItemWriter<GroupPurchaseDocument> groupPurchaseIncrementalReindexWriter
    ) {
        return new StepBuilder("groupPurchaseIncrementalReindex", jobRepository)
                .<GroupPurchaseReindexRow, GroupPurchaseDocument>chunk(properties.getBatchSize(), transactionManager)
                .reader(groupPurchaseIncrementalReindexReader)
                .processor(groupPurchaseIncrementalReindexProcessor)
                .writer(groupPurchaseIncrementalReindexWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<GroupPurchaseReindexRow> groupPurchaseIncrementalReindexReader(
            @Value("#{jobExecutionContext['since']}") String since
    ) {
        return new GroupPurchaseReindexReader(reindexRepository, properties.getBatchSize(), since);
    }

    @Bean
    public ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseDocument> groupPurchaseIncrementalReindexProcessor() {
        return new GroupPurchaseReindexProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter<GroupPurchaseDocument> groupPurchaseIncrementalReindexWriter(
            @Value("#{jobExecutionContext['targetIndex']}") String targetIndex
    ) {
        return new GroupPurchaseReindexWriter(reindexService, targetIndex);
    }
}
