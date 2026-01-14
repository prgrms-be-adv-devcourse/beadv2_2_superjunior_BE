package store._0982.batch.batch.elasticsearch.reindex.config.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.elasticsearch.reindex.config.GroupPurchaseReindexProperties;
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRow;
import store._0982.batch.batch.elasticsearch.reindex.document.GroupPurchaseDocument;
import store._0982.batch.batch.elasticsearch.reindex.processor.GroupPurchaseReindexProcessor;
import store._0982.batch.batch.elasticsearch.reindex.reader.GroupPurchaseReindexReader;
import store._0982.batch.batch.elasticsearch.reindex.service.GroupPurchaseReindexService;
import store._0982.batch.batch.elasticsearch.reindex.writer.GroupPurchaseReindexWriter;
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRepository;

@Configuration
@RequiredArgsConstructor
public class GroupPurchaseFullReindexStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GroupPurchaseReindexProperties properties;
    private final GroupPurchaseReindexRepository reindexRepository;
    private final GroupPurchaseReindexService reindexService;

    @Bean
    public Step groupPurchaseFullReindexStep(
            @Qualifier("groupPurchaseFullReindexReader") ItemReader<GroupPurchaseReindexRow> groupPurchaseFullReindexReader,
            @Qualifier("groupPurchaseFullReindexProcessor") ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseDocument> groupPurchaseReindexProcessor,
            @Qualifier("groupPurchaseFullReindexWriter") ItemWriter<GroupPurchaseDocument> groupPurchaseReindexWriter
    ) {
        return new StepBuilder("groupPurchaseFullReindex", jobRepository)
                .<GroupPurchaseReindexRow, GroupPurchaseDocument>chunk(properties.getBatchSize(), transactionManager)
                .reader(groupPurchaseFullReindexReader)
                .processor(groupPurchaseReindexProcessor)
                .writer(groupPurchaseReindexWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<GroupPurchaseReindexRow> groupPurchaseFullReindexReader() {
        return new GroupPurchaseReindexReader(reindexRepository, properties.getBatchSize(), null);
    }

    @Bean
    public ItemProcessor<GroupPurchaseReindexRow, GroupPurchaseDocument> groupPurchaseFullReindexProcessor() {
        return new GroupPurchaseReindexProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter<GroupPurchaseDocument> groupPurchaseFullReindexWriter(
            @Value("#{jobExecutionContext['targetIndex']}") String targetIndex
    ) {
        return new GroupPurchaseReindexWriter(reindexService, targetIndex);
    }
}
