package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.application.point.PointReturnService;
import store._0982.point.common.KafkaGroupIds;

@Component
@RequiredArgsConstructor
public class OrderCanceledEventListener {

    private final PointReturnService pointReturnService;
    private final PgCancelService pgCancelService;

    @RetryableTopic(exclude = {DuplicateKeyException.class, CustomException.class})
    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELED,
            groupId = KafkaGroupIds.PAYMENT_SERVICE,
            containerFactory = "orderCanceledEventListenerContainerFactory"
    )
    public void handleOrderCanceledEvent(OrderCanceledEvent event) {
        // TODO: 나중에 병목 현상을 막기 위해 포인트 반환 토픽과 PG 환불 토픽을 분리하자
        switch (event.getMethod()) {
            case POINT -> {
                PointReturnCommand command = new PointReturnCommand(
                        event.getEventId(), event.getOrderId(), event.getCancelReason(), event.getAmount());
                pointReturnService.returnPoints(event.getMemberId(), command);
            }
            case PG -> {
                PgCancelCommand command = new PgCancelCommand(
                        event.getOrderId(), event.getCancelReason(), event.getAmount());
                pgCancelService.refundPaymentPoint(event.getMemberId(), command);
            }
            default -> throw new IllegalStateException("Unexpected payment method: " + event.getMethod());
        }
    }
}
