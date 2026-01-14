package store._0982.batch.batch.settlement.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.sellerbalance.SellerBalanceService;
import store._0982.batch.application.settlement.event.SettlementProcessedEvent;
import store._0982.batch.domain.settlement.Settlement;

@Component
@RequiredArgsConstructor
public class SettlementWithdrawalWriterListener implements ItemWriteListener<Settlement> {

    private final SellerBalanceService sellerBalanceService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void afterWrite(Chunk<? extends Settlement> items) {
        items.forEach(settlement -> {
            settlement.markAsCompleted();

            long amount = settlement.getSettlementAmount().longValue();
            sellerBalanceService.saveSellerBalanceHistory(settlement, amount);

            eventPublisher.publishEvent(
                    new SettlementProcessedEvent(settlement));
        });
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Settlement> items) {
        items.forEach(settlement -> {
            settlement.markAsFailed();
            eventPublisher.publishEvent(
                    new SettlementProcessedEvent(settlement));
        });
    }
}
