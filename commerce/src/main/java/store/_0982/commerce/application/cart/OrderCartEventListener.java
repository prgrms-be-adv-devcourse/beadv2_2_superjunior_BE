package store._0982.commerce.application.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.event.OrderCartCompletedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCartEventListener {

    private final CartService cartService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCartCompleted(OrderCartCompletedEvent event){
        cartService.deleteCartById(event.carts());
    }

}
