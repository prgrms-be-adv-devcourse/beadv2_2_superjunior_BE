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

import java.util.List;

@Component
@RequiredArgsConstructor
public class LowBalanceNotificationWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<? extends Settlement> settlements = chunk.getItems();

        for (Settlement settlement : settlements) {
            // Settlement 저장
            settlementRepository.save(settlement);

            // 지연된 이벤트 발행
            eventPublisher.publishEvent(
                    new SettlementProcessedEvent(
                            settlement
                    )
            );
        }
    }
}
