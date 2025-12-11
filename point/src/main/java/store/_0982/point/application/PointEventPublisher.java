package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.PaymentPoint;

@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private final KafkaTemplate<String, PointEvent> kafkaTemplate;

    public void publishPointRechargedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createEvent(paymentPoint, PointEvent.Status.RECHARGED);
        kafkaTemplate.send(KafkaTopics.POINT_RECHARGED, paymentPoint.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createEvent(paymentPoint, PointEvent.Status.DEDUCTED);
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, paymentPoint.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createEvent(paymentPoint, PointEvent.Status.RETURNED);
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, paymentPoint.getMemberId().toString(), event);
    }

    private static PointEvent createEvent(PaymentPoint paymentPoint, PointEvent.Status status) {
        return new PointEvent(
                paymentPoint.getId(),
                paymentPoint.getMemberId(),
                paymentPoint.getAmount(),
                status,
                paymentPoint.getPaymentMethod()
        );
    }
}
