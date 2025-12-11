package store._0982.order.infrastructure.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.order.domain.settlement.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MonthlySettlementJobConfig {

    private static final long MINIMUM_TRANSFER_AMOUNT = 30000L;
    private static final int SERVICE_FEE_RATE = 20;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementFailureRepository settlementFailureRepository;

    @Bean
    public Job monthlySettlementJob(Step monthlySettlementStep) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .start(monthlySettlementStep)
                .build();
    }

    @Bean
    public Step monthlySettlementStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<SellerBalance, Settlement>chunk(10, transactionManager)
                .reader(monthlySettlementReader(entityManagerFactory))
                .processor(monthlySettlementProcessor())
                .writer(monthlySettlementWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<SellerBalance> monthlySettlementReader(
            EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<SellerBalance>()
                .name("monthlySettlementReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString("""
                        SELECT s
                        FROM SellerBalance s
                        WHERE s.settlementBalance >= :amount
                        ORDER BY s.balanceId ASC
                        """)
                .parameterValues(Map.of("amount", MINIMUM_TRANSFER_AMOUNT))
                .build();
    }

    @Bean
    public ItemProcessor<SellerBalance, Settlement> monthlySettlementProcessor() {
        return sellerBalance -> {
            Long currentBalance = sellerBalance.getSettlementBalance();
            long serviceFee = currentBalance / SERVICE_FEE_RATE;
            long transferAmount = currentBalance - serviceFee;

            ZoneId zone = ZoneId.of("Asia/Seoul");
            YearMonth lastMonth = YearMonth.now(zone).minusMonths(1);
            OffsetDateTime periodStart = lastMonth
                    .atDay(1)
                    .atStartOfDay(zone)
                    .toOffsetDateTime();

            OffsetDateTime periodEnd = lastMonth
                    .atEndOfMonth()
                    .atTime(23, 59, 59)
                    .atZone(zone)
                    .toOffsetDateTime();

            return new Settlement(
                    sellerBalance.getMemberId(),
                    periodStart,
                    periodEnd,
                    currentBalance,
                    BigDecimal.valueOf(serviceFee),
                    BigDecimal.valueOf(transferAmount)
            );
        };
    }

    @Bean
    public ItemWriter<Settlement> monthlySettlementWriter() {
        return settlements -> {
            for (Settlement settlement : settlements) {
                try {
                    // TODO : 송금 로직

                    settlement.markAsCompleted();
                    settlementRepository.save(settlement);

                    SellerBalance balance = sellerBalanceRepository
                            .findByMemberId(settlement.getSellerId())
                            .orElseThrow();

                    balance.resetBalance();
                    sellerBalanceRepository.save(balance);

                } catch (Exception e) {
                    settlement.markAsFailed();
                    settlementRepository.save(settlement);

                    SettlementFailure failure = new SettlementFailure(
                            settlement.getSellerId(),
                            settlement.getPeriodStart(),
                            settlement.getPeriodEnd(),
                            e.getMessage(),
                            0,
                            settlement.getSettlementId()
                    );
                    settlementFailureRepository.save(failure);
                }
            }
        };
    }
}
