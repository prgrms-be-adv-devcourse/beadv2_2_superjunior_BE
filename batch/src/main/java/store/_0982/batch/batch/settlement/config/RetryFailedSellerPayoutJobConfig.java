package store._0982.batch.batch.settlement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store._0982.batch.batch.settlement.listener.SellerPayoutJobListener;

/**
 * 출금 정산 재시도 Job 설정
 *
 * retryFailedSellerPayoutStep: 정산 대상 판매자에게 송금 및 정산 처리
 *
 */
@RequiredArgsConstructor
@Configuration
public class RetryFailedSellerPayoutJobConfig {

    private final JobRepository jobRepository;
    private final SellerPayoutJobListener sellerPayoutWithdrawalJobListener;

    private final Step retryFailedSellerPayoutStep;

    @Bean
    public Job retryFailedSellerPayoutJob() {
        return new JobBuilder("retryFailedSellerPayoutJob", jobRepository)
                .incrementer(new TimestampIncrementer())
                .start(retryFailedSellerPayoutStep)
                .listener(sellerPayoutWithdrawalJobListener)
                .build();
    }
}
