package store._0982.batch.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SellerBalanceChangedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerBalanceEventPublisher {

    private final KafkaTemplate<String, SellerBalanceChangedEvent> sellerBalanceKafkaTemplate;

    public void publishSellerBalanceCompletedEvent(SellerBalance sellerBalance, Long amount) {
        SellerBalanceChangedEvent event = sellerBalance.toCompletedEvent(amount);
        send(sellerBalance.getBalanceId().toString(), event);
    }

    private void send(String key, SellerBalanceChangedEvent event) {
        sellerBalanceKafkaTemplate.send(KafkaTopics.SELLER_BALANCE_CHANGED, key, event)
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        log.info("[KAFKA] [{}] successfully sent to partition {}", KafkaTopics.SELLER_BALANCE_CHANGED, result.getRecordMetadata().partition());
                    } else {
                        log.error("[ERROR] [KAFKA] [{}] failed to send after infrastructure retries", KafkaTopics.SELLER_BALANCE_CHANGED, throwable);
                    }
                });
    }
}
