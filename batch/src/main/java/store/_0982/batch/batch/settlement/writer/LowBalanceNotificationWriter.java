package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.settlement.event.SettlementProcessedEvent;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementRepository;
import store._0982.batch.application.settlement.SettlementListener;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LowBalanceNotificationWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<Settlement> settlements = chunk.getItems().stream()
                .map(settlement -> (Settlement) settlement)
                .toList();

        settlementRepository.saveAll(settlements);
        for (Settlement settlement : settlements) {
            eventPublisher.publishEvent(
                    new SettlementProcessedEvent(
                            settlement
                    )
            );
        }
    }
}
