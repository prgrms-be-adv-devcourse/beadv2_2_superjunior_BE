package store._0982.batch.batch.settlement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalReaderListener;
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalStepListener;
import store._0982.batch.batch.settlement.listener.SettlementWithdrawalWriterListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.processor.SettlementWithdrawalProcessor;
import store._0982.batch.batch.settlement.reader.SettlementWithdrawalReader;
import store._0982.batch.batch.settlement.writer.SettlementWithdrawalWriter;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;

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

    private final SettlementWithdrawalReader settlementWithdrawalReader;
    private final SettlementWithdrawalProcessor settlementWithdrawalProcessor;
    private final SettlementWithdrawalWriter settlementWithdrawalWriter;

    private final SettlementWithdrawalStepListener stepListener;
    private final SettlementWithdrawalReaderListener settlementWithdrawalReaderListener;
    private final SettlementWithdrawalWriterListener settlementWithdrawalWriterListener;

    @Bean
    public Step settlementWithdrawalStep() {
        return new StepBuilder("settlementWithdrawalStep", jobRepository)
                .<SellerBalance, Settlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(settlementWithdrawalReader.create())
                .processor(settlementWithdrawalProcessor)
                .writer(settlementWithdrawalWriter)

                .listener(stepListener)
                .listener(settlementWithdrawalReaderListener)
                .listener(settlementWithdrawalWriterListener)
                .build();
    }
}
