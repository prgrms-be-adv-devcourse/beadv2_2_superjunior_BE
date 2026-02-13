package store._0982.batch.batch.sellerpayout.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.sellerpayout.dto.SellerBalanceDto;
import store._0982.batch.batch.sellerpayout.policy.SellerPayoutPolicy;
import store._0982.batch.domain.sellerpayout.SellerPayoutPeriod;
import store._0982.common.domain.sellerpayout.SellerPayout;

@Component
public class LowBalanceNotificationProcessor implements ItemProcessor<SellerBalanceDto, SellerPayout> {

    @Override
    public SellerPayout process(SellerBalanceDto sellerBalance) {
        Long currentBalance = sellerBalance.settlementBalance();

        SellerPayoutPeriod period = SellerPayoutPeriod.ofLastMonth(SellerPayoutPolicy.KOREA_ZONE);

        return SellerPayout.createSellerPayout(
                sellerBalance.memberId(),
                period.start(),
                period.end(),
                currentBalance,
                null,
                null
        );
    }
}
