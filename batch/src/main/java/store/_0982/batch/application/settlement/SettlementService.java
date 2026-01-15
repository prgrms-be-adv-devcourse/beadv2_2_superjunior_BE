package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementFailure;
import store._0982.batch.domain.settlement.SettlementFailureRepository;
import store._0982.batch.domain.settlement.SettlementRepository;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SettlementService {

    private final SettlementFailureRepository settlementFailureRepository;
    private final SettlementRepository settlementRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAllSettlementFailures(List<Settlement> settlements, String reason) {
        settlements.forEach(Settlement::markAsFailed);

        List<SettlementFailure> failures = settlements.stream()
                .map(settlement -> new SettlementFailure(
                        settlement.getSellerId(),
                        settlement.getPeriodStart(),
                        settlement.getPeriodEnd(),
                        reason,
                        0,
                        settlement.getSettlementId()
                ))
                .toList();

        settlementFailureRepository.saveAll(failures);
        settlementRepository.saveAll(settlements);
    }
}
