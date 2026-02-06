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
 * 출금 정산 Job 설정
 *
 * Flow:
 * 1. sellerPayoutStep: 정산 대상 판매자에게 송금 및 정산 처리
 * 2. lowBalanceNotificationStep: 잔액 부족 판매자에게 알림 발송
 *
 * @see SellerPayoutStepConfig
 * @see LowBalanceNotificationStepConfig
 */
@RequiredArgsConstructor
@Configuration
public class SellerPayoutJobConfig {

    private final JobRepository jobRepository;
    private final SellerPayoutJobListener sellerPayoutJobListener;

    private final Step sellerPayoutStep;
    private final Step lowBalanceNotificationStep;

    @Bean
    public Job sellerPayoutJob() {
        return new JobBuilder("sellerPayoutJob", jobRepository)
                .incrementer(new TimestampIncrementer())
                .listener(sellerPayoutJobListener)
                .start(sellerPayoutStep)
                .next(lowBalanceNotificationStep)
                .build();
    }
}
