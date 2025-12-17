package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.entity.PaymentPoint;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointEventPublisher {
    private static final int MAX_RETRY = 3;
    private static final long BASE_DELAY_MS = 1000; // 1초

    private final KafkaTemplate<String, PointEvent> kafkaTemplate;

    public void publishPointRechargedEvent(PaymentPoint paymentPoint) {
        PointEvent event = createPointRechargedEvent(paymentPoint);
        sendWithRetry(KafkaTopics.POINT_RECHARGED, paymentPoint.getMemberId().toString(), event, 0);
    }

    public void publishPointDeductedEvent(MemberPointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.DEDUCTED);
        sendWithRetry(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event, 0);
    }

    public void publishPointReturnedEvent(MemberPointHistory history) {
        PointEvent event = createPointChangedEvent(history, PointEvent.Status.RETURNED);
        sendWithRetry(KafkaTopics.POINT_CHANGED, history.getMemberId().toString(), event, 0);
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

    private void sendWithRetry(String topic, String key, PointEvent event, int retryCount) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[Kafka] {} successfully sent", topic);
                        return;
                    }
                    if (retryCount < MAX_RETRY) {
                        long delayMs = calculateBackoffDelay(retryCount);
                        log.warn("[Kafka] {} failed to send. Retrying in {}ms (attempt {}/{})",
                                topic, delayMs, retryCount + 1, MAX_RETRY);
                        scheduleRetry(topic, key, event, retryCount + 1, delayMs);
                        return;
                    }
                    log.error("[Kafka] {} failed to send after {} retries", topic, MAX_RETRY, throwable);
                });
    }

    private long calculateBackoffDelay(int retryCount) {
        return BASE_DELAY_MS * (1L << retryCount);
    }

    private void scheduleRetry(String topic, String key, PointEvent event, int retryCount, long delayMs) {
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                .execute(() -> sendWithRetry(topic, key, event, retryCount));
    }
}
