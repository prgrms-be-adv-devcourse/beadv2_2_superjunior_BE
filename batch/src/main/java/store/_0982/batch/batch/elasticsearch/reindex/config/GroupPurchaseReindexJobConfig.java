package store._0982.batch.batch.elasticsearch.reindex.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.elasticsearch.reindex.tasklet.CreateGroupPurchaseIndexTasklet;
import store._0982.batch.batch.elasticsearch.reindex.tasklet.GroupPurchaseAliasSwitchTasklet;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GroupPurchaseReindexProperties.class)
public class GroupPurchaseReindexJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GroupPurchaseReindexProperties properties;


    private final Step groupPurchaseFullReindexStep;
    private final Step groupPurchaseIncrementalReindexStep;

    @Bean
    public GroupPurchaseReindexModeDecider groupPurchaseReindexModeDecider() {
        return new GroupPurchaseReindexModeDecider();
    }

    @Bean
    public GroupPurchaseReindexJobListener groupPurchaseReindexJobListener() {
        return new GroupPurchaseReindexJobListener(properties);
    }

    @Bean
    public Step createGroupPurchaseIndexStep(CreateGroupPurchaseIndexTasklet tasklet) {
        return new StepBuilder("createGroupPurchaseIndex", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step groupPurchaseAliasSwitchStep(GroupPurchaseAliasSwitchTasklet tasklet) {
        return new StepBuilder("groupPurchaseAliasSwitch", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job groupPurchaseReindexJob(
            GroupPurchaseReindexModeDecider decider,
            GroupPurchaseReindexJobListener listener,
            Step createGroupPurchaseIndexStep,
            Step groupPurchaseAliasSwitchStep
    ) {
        return new JobBuilder("groupPurchaseReindex", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(decider)
                    .on(GroupPurchaseReindexModeDecider.STATUS_INCREMENTAL)
                    .to(groupPurchaseIncrementalReindexStep)
                .from(decider)
                    .on(GroupPurchaseReindexModeDecider.STATUS_FULL)
                    .to(createGroupPurchaseIndexStep)
                    .next(groupPurchaseFullReindexStep)
                    .next(groupPurchaseIncrementalReindexStep)
                    .next(groupPurchaseAliasSwitchStep)
                .end()
                .build();
    }
}
