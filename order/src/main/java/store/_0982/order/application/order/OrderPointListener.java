package store._0982.order.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.common.event.dto.OrderCreatedEvent;
import store._0982.order.infrastructure.client.payment.PaymentClient;
import store._0982.order.infrastructure.client.payment.dto.PointDeductRequest;

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
