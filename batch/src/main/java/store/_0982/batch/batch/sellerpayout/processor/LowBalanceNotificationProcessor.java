package store._0982.batch.batch.sellerpayout.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.sellerpayout.policy.SellerPayoutPolicy;
import store._0982.batch.domain.sellerpayout.SellerPayoutPeriod;
import store._0982.common.domain.sellerbalance.SellerBalance;
import store._0982.common.domain.sellerpayout.SellerPayout;

@Component
public class LowBalanceNotificationProcessor implements ItemProcessor<SellerBalance, SellerPayout> {

    @Override
    public SellerPayout process(SellerBalance sellerBalance) {
        Long currentBalance = sellerBalance.getSettlementBalance();

        SellerPayoutPeriod period = SellerPayoutPeriod.ofLastMonth(SellerPayoutPolicy.KOREA_ZONE);

        return SellerPayout.createSellerPayout(
                sellerBalance.getMemberId(),
                period.start(),
                period.end(),
                currentBalance,
                null,
                null
        );
    }
}
