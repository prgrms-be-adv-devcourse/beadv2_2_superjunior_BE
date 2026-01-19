package store._0982.batch.batch.grouppurchase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.batch.grouppurchase.listener.GroupPurchaseJobListener;

/**
 * 공동구매 상태 변경 Job 설정
 */
@Configuration
@RequiredArgsConstructor
public class GroupPurchaseJobConfig {

    private final JobRepository jobRepository;
    private final GroupPurchaseJobListener jobListener;

    private final Step openGroupPurchaseStep;
    private final Step updateStatusClosedGroupPurchaseStep;
    private final Step updateStatusOrderStep;

    @Bean
    public Job groupPurchaseJob(){
        return new JobBuilder("groupPurchaseJob",jobRepository )
                .incrementer(new GroupPurchaseJobIncrementer())
                .listener(jobListener)
                .start(openGroupPurchaseStep)
                .next(updateStatusClosedGroupPurchaseStep)
                .next(updateStatusOrderStep)
                .build();
    }

}
