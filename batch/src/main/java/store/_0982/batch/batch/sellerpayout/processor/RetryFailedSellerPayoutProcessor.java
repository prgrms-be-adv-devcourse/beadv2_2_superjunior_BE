package store._0982.batch.batch.sellerpayout.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.sellerpayout.dto.SellerPayoutFailureDto;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;

@RequiredArgsConstructor
@Component
public class RetryFailedSellerPayoutProcessor implements ItemProcessor<SellerPayoutFailureDto, SellerPayout> {

    private final SellerPayoutRepository sellerPayoutRepository;

    @Override
    public SellerPayout process(SellerPayoutFailureDto sellerPayoutFailure) {
        SellerPayout sellerPayout = sellerPayoutRepository.findById(sellerPayoutFailure.sellerPayoutId())
                .orElse(null);

        if (sellerPayout == null) return null;
        if (sellerPayout.isCompleted()) return null;

        return sellerPayout;
    }
}
