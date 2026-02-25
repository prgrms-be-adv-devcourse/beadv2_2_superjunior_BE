package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseParticipatedEvent;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.Product;

@Service
@RequiredArgsConstructor
public class TxParticipateService {

    private final ApplicationEventPublisher eventPublisher;
    private final GroupPurchaseQuantityService groupPurchaseQuantityService;

    @Transactional
    public void afterReserve(GroupPurchase groupPurchase, Product product, int currentCount) {
        eventPublisher.publishEvent(
                new GroupPurchaseParticipatedEvent(groupPurchase, product)
        );
    }
}
