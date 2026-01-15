package store._0982.batch.batch.settlement.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.settlement.listener.LowBalanceNotificationReaderListener;
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalStepListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.processor.LowBalanceNotificationProcessor;
import store._0982.batch.batch.settlement.writer.LowBalanceNotificationWriter;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;

import java.util.Map;

/**
 * 잔액 부족 알림 Step 설정
 * - 최소 송금 금액 미달 판매자 조회
 * - 알림 생성 및 발송
 */
@RequiredArgsConstructor
@Configuration
public class LowBalanceNotificationStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final LowBalanceNotificationProcessor lowBalanceNotificationProcessor;
    private final LowBalanceNotificationWriter lowBalanceNotificationWriter;

    private final SettlementWithdrawalStepListener stepListener;
    private final LowBalanceNotificationReaderListener lowBalanceNotificationReaderListener;

    @Bean
    public Step lowBalanceNotificationStep(
            JpaPagingItemReader<SellerBalance> lowBalanceNotificationReader) {
        return new StepBuilder("lowBalanceNotificationStep", jobRepository)
                .<SellerBalance, Settlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(lowBalanceNotificationReader)
                .processor(lowBalanceNotificationProcessor)
                .writer(lowBalanceNotificationWriter)
                .listener(stepListener)
                .listener(lowBalanceNotificationReaderListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<SellerBalance> lowBalanceNotificationReader() {
        return new JpaPagingItemReaderBuilder<SellerBalance>()
                .name("lowBalanceNotificationReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SettlementPolicy.CHUNK_UNIT)
                .queryString("""
                          SELECT s
                          FROM SellerBalance s
                          WHERE s.settlementBalance > 0
                            AND s.settlementBalance < :amount
                          ORDER BY s.balanceId ASC
                          """)
                .parameterValues(Map.of(
                        "amount", SettlementPolicy.MINIMUM_TRANSFER_AMOUNT
                ))
                .build();
    }
}
