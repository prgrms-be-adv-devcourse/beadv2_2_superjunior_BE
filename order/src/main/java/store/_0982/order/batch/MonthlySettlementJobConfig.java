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
import org.springframework.kafka.core.KafkaTemplate;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementEvent;
import store._0982.order.application.settlement.BankTransferService;
import store._0982.order.domain.settlement.*;
import store._0982.order.client.MemberFeignClient;
import store._0982.order.client.dto.SellerAccountInfo;

import java.math.BigDecimal;
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

    private final MemberFeignClient memberFeignClient;
    private final KafkaTemplate<String, SettlementEvent> settlementKafkaTemplate;

    private final SettlementRepository settlementRepository;
    private final SettlementFailureRepository settlementFailureRepository;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;
    private final BankTransferService bankTransferService;

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
        return settlements -> {

            List<Settlement> settlementList = new ArrayList<>(settlements.getItems());
            List<UUID> sellerIds = settlementList.stream()
                    .map(Settlement::getSellerId)
                    .toList();

//            List<SellerAccountInfo> accountInfos = memberFeignClient.getSellerAccountInfos(sellerIds);
//            Map<UUID, SellerAccountInfo> accountMap = accountInfos.stream()
//                    .collect(Collectors.toMap(SellerAccountInfo::sellerId, Function.identity()));

            Map<UUID, SellerBalance> balanceMap = sellerBalanceRepository
                    .findAllByMemberIdIn(sellerIds)
                    .stream()
                    .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

            for (Settlement settlement : settlementList) {
//                SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());
//
//                if (accountInfo == null || accountInfo.accountNumber() == null || accountInfo.accountNumber().isBlank()) {
//                    handleNoAccountInfo(settlement);
//                    continue;
//                }
//
//                processTransfer(settlement, accountInfo, balanceMap);
                processTransfer(settlement, balanceMap);
            }
        };
    }

    private void handleNoAccountInfo(Settlement settlement) {
        settlement.markAsFailed();
        settlementRepository.save(settlement);

        SettlementFailure failure = new SettlementFailure(
                settlement.getSellerId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                "계좌 정보가 등록되지 않았습니다",
                0,
                settlement.getSettlementId()
        );
        settlementFailureRepository.save(failure);
        publishFailedEvent(settlement);
    }

    private void processTransfer(Settlement settlement, Map<UUID, SellerBalance> balanceMap) {
        try {
            long transferAmount = settlement.getSettlementAmount().longValue();
//            bankTransferService.transfer(accountInfo, transferAmount);

            settlement.markAsCompleted();
            settlementRepository.save(settlement);

            SellerBalanceHistory history = new SellerBalanceHistory(
                    settlement.getSellerId(),
                    settlement.getSettlementId(),
                    transferAmount,
                    BalanceHistoryStatus.DEBIT
            );
            sellerBalanceHistoryRepository.save(history);

            SellerBalance balance = balanceMap.get(settlement.getSellerId());
            balance.resetBalance();
            sellerBalanceRepository.save(balance);

            publishCompletedEvent(settlement);
            log.info("[MONTHLY_SETTLEMENT] 송금 성공 - 판매자: {}, 금액: {}", settlement.getSellerId(), transferAmount);

        } catch (Exception e) {
            log.error("[MONTHLY_SETTLEMENT] 송금 실패 - 판매자: {}, 오류: {}", settlement.getSellerId(), e.getMessage());

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

            publishFailedEvent(settlement);
        }
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
                .parameterValues(Map.of("minAmount", MINIMUM_TRANSFER_AMOUNT))
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
        return settlements -> {
            List<Settlement> settlementList = new ArrayList<>(settlements.getItems());

            for (Settlement settlement : settlementList) {
                settlement.markAsFailed();
                settlementRepository.save(settlement);
                publishDeferredEvent(settlement);
            }
        };
    }

    private void publishCompletedEvent(Settlement settlement) {
        SettlementEvent event = settlement.toCompletedEvent();
        settlementKafkaTemplate.send(
                KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED,
                settlement.getSellerId().toString(),
                event
        );
    }

    private void publishFailedEvent(Settlement settlement) {
        SettlementEvent event = settlement.toFailedEvent();
        settlementKafkaTemplate.send(
                KafkaTopics.MONTHLY_SETTLEMENT_FAILED,
                settlement.getSellerId().toString(),
                event
        );
    }

    private void publishDeferredEvent(Settlement settlement) {
        SettlementEvent event = settlement.toDeferredEvent();
        settlementKafkaTemplate.send(
                KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED,
                settlement.getSellerId().toString(),
                event
        );
    }
}
