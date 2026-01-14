package store._0982.batch.batch.settlement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.settlement.listener.LowBalanceNotificationItemReaderListener;
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalStepListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.processor.LowBalanceNotificationProcessor;
import store._0982.batch.batch.settlement.reader.lowBalanceNotificationReader;
import store._0982.batch.batch.settlement.writer.LowBalanceNotificationWriter;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;

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

    private final lowBalanceNotificationReader lowBalanceNotificationReader;
    private final LowBalanceNotificationProcessor lowBalanceNotificationProcessor;
    private final LowBalanceNotificationWriter lowBalanceNotificationWriter;

    private final SettlementWithdrawalStepListener stepListener;
    private final LowBalanceNotificationItemReaderListener lowBalanceItemReaderListener;

    @Bean
    public Step lowBalanceNotificationStep() {
        return new StepBuilder("lowBalanceNotificationStep", jobRepository)
                .<SellerBalance, Settlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(lowBalanceNotificationReader.create())
                .processor(lowBalanceNotificationProcessor)
                .writer(lowBalanceNotificationWriter)

                .listener(stepListener)
                .listener(lowBalanceItemReaderListener)
                .build();
    }
}
