package store._0982.order.batch;

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
import store._0982.order.client.dto.SellerAccountListRequest;
import store._0982.order.infrastructure.settlement.event.SettlementEventPublisher;
import store._0982.order.domain.settlement.*;
import store._0982.order.client.MemberFeignClient;
import store._0982.order.client.dto.SellerAccountInfo;

import java.math.BigDecimal;
import java.time.ZoneId;
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

    private final MemberFeignClient memberFeignClient;
    private final MonthlySettlementProcessor monthlySettlementProcessor;
    private final SettlementEventPublisher settlementEventPublisher;

    private final SettlementRepository settlementRepository;
    private final SellerBalanceRepository sellerBalanceRepository;

    @Bean
    public Job monthlySettlementJob(Step monthlySettlementStep, Step lowBalanceNotificationStep) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .start(monthlySettlementStep)
                .next(lowBalanceNotificationStep)
                .build();
    }

    @Bean
    public Step monthlySettlementStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<SellerBalance, Settlement>chunk(10, transactionManager)
                .reader(monthlySettlementReader(entityManagerFactory))
                .processor(settlementItemProcessor())
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
                .parameterValues(Map.of(
                        "amount", MINIMUM_TRANSFER_AMOUNT
                ))
                .build();
    }

    @Bean
    public ItemProcessor<SellerBalance, Settlement> settlementItemProcessor() {
        return sellerBalance -> {
            Long currentBalance = sellerBalance.getSettlementBalance();
            long serviceFee = currentBalance / SERVICE_FEE_RATE;
            long transferAmount = currentBalance - serviceFee;

            SettlementPeriod period = SettlementPeriod.ofLastMonth(ZoneId.of("Asia/Seoul"));

            return new Settlement(
                    sellerBalance.getMemberId(),
                    period.start(),
                    period.end(),
                    currentBalance,
                    BigDecimal.valueOf(serviceFee),
                    BigDecimal.valueOf(transferAmount)
            );
        };
    }

    @Bean
    public ItemWriter<Settlement> monthlySettlementWriter() {
        return chunk -> {

            List<? extends Settlement> settlements = chunk.getItems();
            List<UUID> sellerIds = settlements.stream()
                    .map(Settlement::getSellerId)
                    .toList();

            SellerAccountListRequest request = new SellerAccountListRequest(sellerIds);
            List<SellerAccountInfo> accountInfos = memberFeignClient.getSellerAccountInfos(request).data();
            Map<UUID, SellerAccountInfo> accountMap = accountInfos.stream()
                    .collect(Collectors.toMap(SellerAccountInfo::sellerId, Function.identity()));

            Map<UUID, SellerBalance> balanceMap = sellerBalanceRepository
                    .findAllByMemberIdIn(sellerIds)
                    .stream()
                    .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

            for (Settlement settlement : settlements) {
                SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());

                if (accountInfo == null || accountInfo.accountNumber() == null || accountInfo.accountNumber().isBlank()) {
                    monthlySettlementProcessor.handleSettlementFailure(settlement, "계좌 정보가 없습니다");
                    continue;
                }

                monthlySettlementProcessor.processTransfer(settlement, accountInfo, balanceMap);
            }
        };
    }

    // 3만원 미만 판매자 알림 Step
    @Bean
    public Step lowBalanceNotificationStep(EntityManagerFactory entityManagerFactory) {
        return new StepBuilder("lowBalanceNotificationStep", jobRepository)
                .<SellerBalance, Settlement>chunk(10, transactionManager)
                .reader(lowBalanceReader(entityManagerFactory))
                .processor(lowBalanceProcessor())
                .writer(lowBalanceNotificationWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<SellerBalance> lowBalanceReader(
            EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<SellerBalance>()
                .name("lowBalanceReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(10)
                .queryString("""
                        SELECT s
                        FROM SellerBalance s
                        WHERE s.settlementBalance > 0
                          AND s.settlementBalance < :minAmount
                        ORDER BY s.balanceId ASC
                        """)
                .parameterValues(Map.of(
                        "minAmount", MINIMUM_TRANSFER_AMOUNT
                ))
                .build();
    }

    @Bean
    public ItemProcessor<SellerBalance, Settlement> lowBalanceProcessor() {
        return sellerBalance -> {
            Long currentBalance = sellerBalance.getSettlementBalance();
            SettlementPeriod period = SettlementPeriod.ofLastMonth(ZoneId.of("Asia/Seoul"));

            return new Settlement(
                    sellerBalance.getMemberId(),
                    period.start(),
                    period.end(),
                    currentBalance,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        };
    }

    @Bean
    public ItemWriter<Settlement> lowBalanceNotificationWriter() {
        return chunk -> {
            List<? extends Settlement> settlements = chunk.getItems();

            for (Settlement settlement : settlements) {
                settlementRepository.save(settlement);
                settlementEventPublisher.publishDeferred(settlement);
            }
        };
    }
}
