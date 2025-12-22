package store._0982.order.settlement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import store._0982.order.settlement.processor.LowBalanceProcessor;
import store._0982.order.settlement.processor.MonthlySettlementProcessor;
import store._0982.order.settlement.reader.LowBalanceReader;
import store._0982.order.settlement.reader.MonthlySettlementReader;
import store._0982.order.settlement.writer.LowBalanceNotificationWriter;
import store._0982.order.settlement.writer.MonthlySettlementWriter;
import store._0982.order.domain.settlement.SellerBalance;
import store._0982.order.domain.settlement.Settlement;

@RequiredArgsConstructor
@Configuration
public class MonthlySettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // Reader
    private final MonthlySettlementReader monthlySettlementReader;
    private final LowBalanceReader lowBalanceReader;

    // Processor
    private final MonthlySettlementProcessor monthlySettlementProcessor;
    private final LowBalanceProcessor lowBalanceProcessor;

    // Writer
    private final MonthlySettlementWriter monthlySettlementWriter;
    private final LowBalanceNotificationWriter lowBalanceNotificationWriter;

    @Bean
    public Job monthlySettlementJob() {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .start(monthlySettlementStep())
                .next(lowBalanceNotificationStep())
                .build();
    }

    @Bean
    public Step monthlySettlementStep() {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<SellerBalance, Settlement>chunk(10, transactionManager)
                .reader(monthlySettlementReader.create())
                .processor(monthlySettlementProcessor)
                .writer(monthlySettlementWriter)
                .build();
    }

    @Bean
    public Step lowBalanceNotificationStep() {
        return new StepBuilder("lowBalanceNotificationStep", jobRepository)
                .<SellerBalance, Settlement>chunk(10, transactionManager)
                .reader(lowBalanceReader.create())
                .processor(lowBalanceProcessor)
                .writer(lowBalanceNotificationWriter)
                .build();
    }
}
