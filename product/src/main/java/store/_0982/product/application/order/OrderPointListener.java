package store._0982.product.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.product.event.dto.OrderCreatedEvent;
import store._0982.product.infrastructure.client.payment.PaymentClient;
import store._0982.product.infrastructure.client.payment.dto.PointDeductRequest;

@Component
@RequiredArgsConstructor
public class OrderPointListener {
    private final PaymentClient paymentClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createOrder(OrderCreatedEvent event){
        paymentClient.deductPointsInternal(
                event.memberId(),
                new PointDeductRequest(
                        event.idempotencyKey(),
                        event.orderId(),
                        event.amount()
                )
        );
    }
}
