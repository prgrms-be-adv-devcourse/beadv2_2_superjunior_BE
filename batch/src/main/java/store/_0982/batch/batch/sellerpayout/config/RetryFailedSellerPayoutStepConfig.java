package store._0982.batch.batch.sellerpayout.config;

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
import store._0982.batch.batch.sellerpayout.listener.SellerPayoutStepListener;
import store._0982.batch.batch.sellerpayout.policy.SellerPayoutPolicy;
import store._0982.batch.batch.sellerpayout.processor.RetryFailedSellerPayoutProcessor;
import store._0982.batch.batch.sellerpayout.writer.RetryFailedSellerPayoutWriter;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutFailure;
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
public class RetryFailedSellerPayoutStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final RetryFailedSellerPayoutProcessor retryFailedSellerPayoutProcessor;
    private final RetryFailedSellerPayoutWriter retryFailedSellerPayoutWriter;

    private final SellerPayoutStepListener stepListener;

    @Bean
    public Step retryFailedSellerPayoutStep(
            JpaPagingItemReader<SellerPayoutFailure> retryFailedSellerPayoutReader) {
        return new StepBuilder("retryFailedSellerPayoutStep", jobRepository)
                .<SellerPayoutFailure, SellerPayout>chunk(SellerPayoutPolicy.CHUNK_UNIT, transactionManager)
                .reader(retryFailedSellerPayoutReader)
                .processor(retryFailedSellerPayoutProcessor)
                .writer(retryFailedSellerPayoutWriter)
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
    public JpaPagingItemReader<SellerPayoutFailure> retryFailedSellerPayoutReader() {
        return new JpaPagingItemReaderBuilder<SellerPayoutFailure>()
                .name("retryFailedSellerPayoutReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(SellerPayoutPolicy.CHUNK_UNIT)
                .queryString("""
                          SELECT sf
                          FROM SellerPayoutFailure sf
                          WHERE sf.retryCount < :maxRetry
                          ORDER BY sf.createdAt ASC
                          """)
                .parameterValues(Map.of(
                        "maxRetry", SellerPayoutPolicy.MAX_RETRY
                ))
                .build();
    }
}
