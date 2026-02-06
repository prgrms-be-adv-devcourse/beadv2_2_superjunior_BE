package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.settlement.SellerPayoutFailureRepository;
import store._0982.common.domain.settlement.SellerPayout;
import store._0982.common.domain.settlement.SellerPayoutFailure;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SellerPayoutService {

    private final SellerPayoutFailureRepository settlementFailureRepository;

    @Transactional
    public void saveSellerPayoutFailure(SellerPayout settlement, String reason) {
        SellerPayoutFailure failure = SellerPayoutFailure.createSellerPayoutFailure(
                settlement.getSellerPayoutId(),
                settlement.getSellerId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                reason,
                0
        );
        settlementFailureRepository.save(failure);
    }
}
