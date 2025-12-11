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
import store._0982.order.application.settlement.BankTransferService;
import store._0982.order.domain.settlement.*;
import store._0982.order.infrastructure.client.MemberFeignClient;
import store._0982.order.infrastructure.client.dto.SellerAccountInfo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final MemberFeignClient memberFeignClient;
    private final BankTransferService bankTransferService;

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

            List<Settlement> settlementList = new ArrayList<>(settlements.getItems());
            List<UUID> sellerIds = settlementList.stream()
                    .map(Settlement::getSellerId)
                    .toList();

            List<SellerAccountInfo> accountInfos = memberFeignClient.getSellerAccountInfos(sellerIds);
            Map<UUID, SellerAccountInfo> accountMap = accountInfos.stream()
                    .collect(Collectors.toMap(SellerAccountInfo::sellerId, Function.identity()));

            Map<UUID, SellerBalance> balanceMap = sellerBalanceRepository
                    .findAllByMemberIdIn(sellerIds)
                    .stream()
                    .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

            for (Settlement settlement : settlementList) {
                try {
                    SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());

                    long transferAmount = settlement.getSettlementAmount().longValue();
                    bankTransferService.transfer(accountInfo, transferAmount);

                    settlement.markAsCompleted();
                    settlementRepository.save(settlement);

                    SellerBalance balance = balanceMap.get(settlement.getSellerId());
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
