package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.PaymentMethod;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.PaymentChangedEvent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPaymentProcessorService {

    private final OrderRepository orderRepository;

    @Transactional
    public void processPaymentStatusUpdate(PaymentChangedEvent event){
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        switch(event.getStatus()){
            case PAYMENT_COMPLETED -> {
                order.completePayment(PaymentMethod.PG);
            }
            case PAYMENT_FAILED -> {
                // TODO: 재시도 로직을 한다면 바로 상태 변경 X
                //order.markFailed();
            }
            case REFUNDED -> {

            }
        }
    }
}
