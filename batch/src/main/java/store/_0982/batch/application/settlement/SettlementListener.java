package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.application.sellerbalance.SellerBalanceService;
import store._0982.batch.application.settlement.event.SettlementCompletedEvent;
import store._0982.batch.application.settlement.event.SettlementFailedEvent;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementStatus;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementDoneEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementListener {

    private final SellerBalanceService sellerBalanceService;
    private final KafkaTemplate<String, SettlementDoneEvent> settlementKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SettlementCompletedEvent event) {
        Settlement settlement = event.settlement();
        sellerBalanceService.clearBalance(settlement);

        SettlementDoneEvent kafkaEvent = settlement.toEvent(SettlementDoneEvent.Status.SUCCESS);
        settlementKafkaTemplate.send(
                KafkaTopics.SETTLEMENT_DONE,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SettlementFailedEvent event) {
        Settlement settlement = event.settlement();

        SettlementDoneEvent kafkaEvent = settlement.toEvent(SettlementDoneEvent.Status.FAILED);
        settlementKafkaTemplate.send(
                KafkaTopics.SETTLEMENT_DONE,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }
}
