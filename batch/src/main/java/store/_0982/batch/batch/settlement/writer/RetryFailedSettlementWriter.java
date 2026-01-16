package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementFailureRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryFailedSettlementWriter implements ItemWriter<Settlement> {

    private final SettlementWithdrawalWriter settlementWithdrawalWriter;
    private final SettlementFailureRepository settlementFailureRepository;

    @Transactional
    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        settlementWithdrawalWriter.write(chunk);

        for (Settlement settlement : chunk) {
            if (settlement.isCompleted()) {
                settlementFailureRepository.deleteBySettlementId(settlement.getSettlementId());
            } else {
                settlementFailureRepository.incrementRetryCount(settlement.getSettlementId());
            }
        }
    }
}
