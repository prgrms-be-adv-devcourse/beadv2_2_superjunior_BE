package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;

@Component
@RequiredArgsConstructor
public class OrderCanceledEventListener {

    @RetryableTopic
    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELED
    )
    public void handleOrderCanceledEvent() {

    }
}
