package store._0982.batch.batch.settlement.config;

import feign.FeignException;
import feign.RetryableException;
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
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalReaderListener;
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalStepListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.processor.SettlementWithdrawalProcessor;
import store._0982.batch.batch.settlement.writer.SettlementWithdrawalWriter;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.common.exception.CustomException;

import java.util.Map;

/**
 * 출금 정산 Step 설정
 * - 정산 대상 판매자 조회
 * - 정산 금액 계산
 * - 은행 송금 및 정산 기록
 */
@RequiredArgsConstructor
@Configuration
public class SettlementWithdrawalStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final SettlementWithdrawalProcessor settlementWithdrawalProcessor;
    private final SettlementWithdrawalWriter settlementWithdrawalWriter;

    private final SettlementWithdrawalStepListener stepListener;
    private final SettlementWithdrawalReaderListener settlementWithdrawalReaderListener;

    @Bean
    public Step settlementWithdrawalStep(
            JpaPagingItemReader<SellerBalance> settlementWithdrawalReader) {
        return new StepBuilder("settlementWithdrawalStep", jobRepository)
                .<SellerBalance, Settlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(settlementWithdrawalReader)
                .processor(settlementWithdrawalProcessor)
                .writer(settlementWithdrawalWriter)
                .listener(stepListener)
                .listener(settlementWithdrawalReaderListener)
                // 재시도 정책
                .faultTolerant()
                .retry(RetryableException.class)
                .retry(FeignException.class)
                .retryLimit(3)
                .noRetry(CustomException.class)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<SellerBalance> settlementWithdrawalReader() {
        return new JpaPagingItemReaderBuilder<SellerBalance>()
                .name("settlementWithdrawalReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SettlementPolicy.CHUNK_UNIT)
                .queryString("""
                          SELECT s
                          FROM SellerBalance s
                          WHERE s.settlementBalance >= :amount
                          ORDER BY s.balanceId ASC
                          """)
                .parameterValues(Map.of(
                        "amount", SettlementPolicy.MINIMUM_TRANSFER_AMOUNT
                ))
                .build();
    }
}
