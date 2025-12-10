package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.PaymentPoint;

@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private static final String EVENT_ID = "eventId";

    private final KafkaTemplate<String, PointEvent> kafkaTemplate;

    public void publishPointRechargedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createEvent(paymentPoint, PointEvent.Status.RECHARGED);
        MDC.put(EVENT_ID, String.valueOf(event.getEventId()));
        kafkaTemplate.send(KafkaTopics.POINT_RECHARGED, paymentPoint.getMemberId().toString(), event);
    }

    public void publishPointDeductedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createEvent(paymentPoint, PointEvent.Status.DEDUCTED);
        MDC.put(EVENT_ID, String.valueOf(event.getEventId()));
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, paymentPoint.getMemberId().toString(), event);
    }

    public void publishPointReturnedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createEvent(paymentPoint, PointEvent.Status.RETURNED);
        MDC.put(EVENT_ID, String.valueOf(event.getEventId()));
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
