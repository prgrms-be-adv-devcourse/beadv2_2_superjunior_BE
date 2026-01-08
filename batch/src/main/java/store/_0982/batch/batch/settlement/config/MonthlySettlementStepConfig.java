package store._0982.batch.batch.settlement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import store._0982.batch.batch.settlement.listener.MonthlySettlementStepListener;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.batch.settlement.processor.MonthlySettlementProcessor;
import store._0982.batch.batch.settlement.reader.MonthlySettlementReader;
import store._0982.batch.batch.settlement.writer.MonthlySettlementWriter;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;

/**
 * 월간 정산 Step 설정
 * - 정산 대상 판매자 조회
 * - 정산 금액 계산
 * - 은행 송금 및 정산 기록
 */
@RequiredArgsConstructor
@Configuration
public class MonthlySettlementStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final MonthlySettlementReader monthlySettlementReader;
    private final MonthlySettlementProcessor monthlySettlementProcessor;
    private final MonthlySettlementWriter monthlySettlementWriter;
    private final MonthlySettlementStepListener stepListener;

    @Bean
    public Step monthlySettlementStep() {
        return new StepBuilder("monthlySettlementStep", jobRepository)
                .<SellerBalance, Settlement>chunk(SettlementPolicy.CHUNK_UNIT, transactionManager)
                .reader(monthlySettlementReader.create())
                .processor(monthlySettlementProcessor)
                .writer(monthlySettlementWriter)
                .listener(stepListener)
                .build();
    }
}
