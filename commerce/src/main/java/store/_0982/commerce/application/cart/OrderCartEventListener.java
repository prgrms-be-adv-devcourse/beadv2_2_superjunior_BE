package store._0982.commerce.application.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.order.event.OrderCartCompletedEvent;

@Component
@RequiredArgsConstructor
public class OrderCartEventListener {

    private final CartService cartService;

    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCartCompleted(OrderCartCompletedEvent event){
        cartService.deleteCartById(event.carts());
    }

}
