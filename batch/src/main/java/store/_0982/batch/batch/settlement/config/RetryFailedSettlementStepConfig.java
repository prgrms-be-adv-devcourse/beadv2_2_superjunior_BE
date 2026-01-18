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
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalStepListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.processor.RetryFailedSettlementProcessor;
import store._0982.batch.batch.settlement.writer.RetryFailedSettlementWriter;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementFailure;
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
public class RetryFailedSettlementStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final RetryFailedSettlementProcessor retryFailedSettlementProcessor;
    private final RetryFailedSettlementWriter retryFailedSettlementWriter;

    private final SettlementWithdrawalStepListener stepListener;

    @Bean
    public Step retryFailedSettlementStep(
            JpaPagingItemReader<SettlementFailure> retryFailedSettlementReader) {
        return new StepBuilder("retryFailedSettlementStep", jobRepository)
                .<SettlementFailure, Settlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(retryFailedSettlementReader)
                .processor(retryFailedSettlementProcessor)
                .writer(retryFailedSettlementWriter)
                .listener(stepListener)
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
    public JpaPagingItemReader<SettlementFailure> retryFailedSettlementReader() {
        return new JpaPagingItemReaderBuilder<SettlementFailure>()
                .name("retryFailedSettlementReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SettlementPolicy.CHUNK_UNIT)
                .queryString("""
                          SELECT sf
                          FROM SettlementFailure sf
                          WHERE sf.retryCount < :maxRetry
                          ORDER BY sf.createdAt ASC
                          """)
                .parameterValues(Map.of(
                        "maxRetry", SettlementPolicy.MAX_RETRY
                ))
                .build();
    }
}
