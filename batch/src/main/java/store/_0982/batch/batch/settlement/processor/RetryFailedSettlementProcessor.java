package store._0982.batch.batch.settlement.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.settlement.SettlementRepository;
import store._0982.common.domain.settlement.Settlement;
import store._0982.common.domain.settlement.SettlementFailure;

@RequiredArgsConstructor
@Component
public class RetryFailedSettlementProcessor implements ItemProcessor<SettlementFailure, Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public Settlement process(SettlementFailure settlementFailure) {
        Settlement settlement = settlementRepository.findById(settlementFailure.getSettlementId())
                .orElse(null);

        if (settlement == null) return null;
        if (settlement.isCompleted()) return null;

        return settlement;
    }
}
