package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.entity.PaymentPoint;

@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private final KafkaTemplate<String, PointEvent> kafkaTemplate;

    public void publishPointRechargedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createPointRechargedEvent(paymentPoint);
        kafkaTemplate.send(KafkaTopics.POINT_RECHARGED, paymentPoint.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(MemberPointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.DEDUCTED);
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(MemberPointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.RETURNED);
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event);
    }

    private static PointEvent createPointRechargedEvent(PaymentPoint paymentPoint) {
        return new PointEvent(
                paymentPoint.getId(),
                paymentPoint.getMemberId(),
                paymentPoint.getAmount(),
                PointEvent.Status.RECHARGED,
                paymentPoint.getPaymentMethod()
        );
    }

    private static PointEvent createPointChangedEvent(MemberPointHistory history, PointEvent.Status status) {
        return new PointEvent(
                history.getId(),
                history.getMemberId(),
                history.getAmount(),
                status,
                null
        );
    }
}
