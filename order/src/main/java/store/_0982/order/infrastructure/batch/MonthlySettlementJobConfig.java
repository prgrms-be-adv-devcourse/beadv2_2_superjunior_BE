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

            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            OffsetDateTime periodStart = lastMonth.atDay(1).atStartOfDay()
                    .atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime periodEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59)
                    .atOffset(OffsetDateTime.now().getOffset());

            Settlement settlement = new Settlement(
                    sellerBalance.getMemberId(),
                    periodStart,
                    periodEnd,
                    currentBalance,
                    BigDecimal.valueOf(serviceFee),
                    BigDecimal.valueOf(transferAmount)
            );

            try {
                log.info("[BATCH] [Seller:{}] 송금 시도", sellerBalance.getMemberId());
                // TODO: 은행 송금 로직

                settlement.markAsCompleted();
                sellerBalance.resetBalance();
                sellerBalanceRepository.save(sellerBalance);

                log.info("[BATCH] [Seller:{}] 송금 성공", sellerBalance.getMemberId());

            } catch (Exception e) {
                log.error("[BATCH] [Seller:{}] 송금 실패", sellerBalance.getMemberId());

                settlement.markAsFailed();

                SettlementFailure failure = new SettlementFailure(
                        sellerBalance.getMemberId(),
                        periodStart,
                        periodEnd,
                        e.getMessage(),
                        0,
                        settlement.getSettlementId()
                );
                settlementFailureRepository.save(failure);
            }

            return settlement;
        };
    }

    @Bean
    public ItemWriter<Settlement> monthlySettlementWriter() {
        return items -> {
            for (Settlement settlement : items) {
                settlementRepository.save(settlement);
            }
        };
    }
}
