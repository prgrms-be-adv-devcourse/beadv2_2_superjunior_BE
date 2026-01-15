package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementFailure;
import store._0982.batch.domain.settlement.SettlementFailureRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SettlementService {

    private final SettlementFailureRepository settlementFailureRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSettlementFailure(Settlement settlement, String reason) {
        settlement.markAsFailed();
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
