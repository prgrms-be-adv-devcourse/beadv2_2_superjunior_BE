package store._0982.batch.batch.grouppurchase.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.grouppurchase.policy.GroupPurchasePolicy;
import store._0982.batch.batch.grouppurchase.processor.UpdateStatusOrderProcessor;
import store._0982.batch.batch.grouppurchase.writer.UpdateStatusOrderWriter;
import store._0982.batch.domain.order.Order;

@Configuration
@RequiredArgsConstructor
public class UpdateStatusOrderStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UpdateStatusOrderProcessor updateStatusOrderProcessor;
    private final UpdateStatusOrderWriter updateStatusOrderWriter;

    @Bean
    public Step updateStatusOrderStep(
            JpaPagingItemReader<Order> updateStatusOrderReader
    ){
        return new StepBuilder("updateStatusOrderStep", jobRepository)
                .<Order, Order>chunk(GroupPurchasePolicy.Order.CHUNK_UNIT, transactionManager)
                .reader(updateStatusOrderReader)
                .processor(updateStatusOrderProcessor)
                .writer(updateStatusOrderWriter)
                .build();
    }


}
