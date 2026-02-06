package store._0982.batch.batch.settlement.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.settlement.policy.SellerPayoutPolicy;
import store._0982.batch.domain.settlement.SellerPayoutPeriod;
import store._0982.common.domain.sellerbalance.SellerBalance;
import store._0982.common.domain.settlement.SellerPayout;

@Component
public class SellerPayoutProcessor implements ItemProcessor<SellerBalance, SellerPayout> {

    @Override
    public SellerPayout process(SellerBalance sellerBalance) {
        Long currentBalance = sellerBalance.getSettlementBalance();

        long serviceFee = SellerPayoutPolicy.calculateServiceFee(currentBalance);
        long transferAmount = SellerPayoutPolicy.calculateTransferAmount(currentBalance);

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
