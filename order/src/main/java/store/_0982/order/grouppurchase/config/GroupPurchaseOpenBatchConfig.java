package store._0982.order.grouppurchase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.order.grouppurchase.config.tasklet.OpenGroupPurchaseTasklet;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;

@Configuration
@RequiredArgsConstructor
public class GroupPurchaseOpenBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Bean
    public Job openGroupPurchaseJob(){
        return new JobBuilder("openGroupPurchase",jobRepository )
                .start(openGroupPurchaseStep())
                .build();
    }

    @Bean
    public Step openGroupPurchaseStep(){
        return new StepBuilder("openGroupPurchase", jobRepository)
                .tasklet(openGroupPurchaseTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet openGroupPurchaseTasklet(){
        return new OpenGroupPurchaseTasklet(groupPurchaseRepository, eventPublisher);
    }
}
