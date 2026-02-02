package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.settlement.SettlementFailureRepository;
import store._0982.common.domain.settlement.Settlement;
import store._0982.common.domain.settlement.SettlementFailure;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SettlementService {

    private final SettlementFailureRepository settlementFailureRepository;

    @Transactional
    public void saveSettlementFailure(Settlement settlement, String reason) {
        SettlementFailure failure = new SettlementFailure(
                settlement.getSellerId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                reason,
                0,
                settlement.getSettlementId()
        );
        settlementFailureRepository.save(failure);
    }
}
