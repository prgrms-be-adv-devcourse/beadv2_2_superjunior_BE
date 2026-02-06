package store._0982.batch.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.common.domain.settlement.SellerPayout;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SellerPayoutDoneEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerPayoutEventPublisher {

    private final KafkaTemplate<String, SellerPayoutDoneEvent> settlementKafkaTemplate;

    public void publishSellerPayoutCompletedEvent(SellerPayout settlement) {
        SellerPayoutDoneEvent event = settlement.toCompletedEvent();
        send(settlement.getSellerPayoutId().toString(), event);
    }

    public void publishSellerPayoutFailedEvent(SellerPayout settlement) {
        SellerPayoutDoneEvent event = settlement.toFailedEvent();
        send(settlement.getSellerPayoutId().toString(), event);
    }

    public void publishSellerPayoutDeferredEvent(SellerPayout settlement) {
        SellerPayoutDoneEvent event = settlement.toDeferredEvent();
        send(settlement.getSellerPayoutId().toString(), event);
    }

    private void send(String key, SellerPayoutDoneEvent event) {
        settlementKafkaTemplate.send(KafkaTopics.SELLER_PAYOUT_DONE, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[KAFKA] [{}] successfully sent to partition {}", KafkaTopics.SELLER_PAYOUT_DONE, result.getRecordMetadata().partition());
                    } else {
                        log.error("[ERROR] [KAFKA] [{}] failed to send after infrastructure retries", KafkaTopics.SELLER_PAYOUT_DONE, throwable);
                    }
                });
    }
}
