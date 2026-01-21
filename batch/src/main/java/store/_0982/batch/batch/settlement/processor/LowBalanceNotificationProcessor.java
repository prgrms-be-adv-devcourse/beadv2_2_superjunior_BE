package store._0982.batch.batch.settlement.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementPeriod;

import java.math.BigDecimal;

@Component
public class LowBalanceNotificationProcessor implements ItemProcessor<SellerBalance, Settlement> {

    @Override
    public Settlement process(SellerBalance sellerBalance) {
        Long currentBalance = sellerBalance.getSettlementBalance();

        SettlementPeriod period = SettlementPeriod.ofLastMonth(SettlementPolicy.KOREA_ZONE);

        return new Settlement(
                sellerBalance.getMemberId(),
                period.start(),
                period.end(),
                currentBalance,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null
        );
    }
}
