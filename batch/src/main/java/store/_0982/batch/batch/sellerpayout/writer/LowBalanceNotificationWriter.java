package store._0982.batch.batch.sellerpayout.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.sellerpayout.event.SellerPayoutDeferredEvent;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LowBalanceNotificationWriter implements ItemWriter<SellerPayout> {

    private final SellerPayoutRepository sellerPayoutRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends SellerPayout> chunk) {
        List<SellerPayout> sellerPayouts = chunk.getItems().stream()
                .map(sellerPayout -> (SellerPayout) sellerPayout)
                .toList();

        for (SellerPayout sellerPayout : sellerPayouts) {
            sellerPayout.markAsDeferred();
            eventPublisher.publishEvent(
                    new SellerPayoutDeferredEvent(sellerPayout)
            );
        }
        sellerPayoutRepository.saveAll(sellerPayouts);
    }
}
