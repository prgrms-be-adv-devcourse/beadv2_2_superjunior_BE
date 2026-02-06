package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.settlement.event.SellerPayoutDeferredEvent;
import store._0982.batch.domain.settlement.SellerPayoutRepository;
import store._0982.common.domain.settlement.SellerPayout;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LowBalanceNotificationWriter implements ItemWriter<SellerPayout> {

    private final SellerPayoutRepository settlementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends SellerPayout> chunk) {
        List<SellerPayout> settlements = chunk.getItems().stream()
                .map(settlement -> (SellerPayout) settlement)
                .toList();

        for (SellerPayout settlement : settlements) {
            settlement.markAsDeferred();
            eventPublisher.publishEvent(
                    new SellerPayoutDeferredEvent(settlement)
            );
        }
        settlementRepository.saveAll(settlements);
    }
}
