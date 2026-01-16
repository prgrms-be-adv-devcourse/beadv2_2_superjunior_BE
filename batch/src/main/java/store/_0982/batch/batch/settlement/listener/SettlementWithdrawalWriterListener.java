package store._0982.batch.batch.settlement.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.settlement.Settlement;

@Component
@RequiredArgsConstructor
public class SettlementWithdrawalWriterListener implements ItemWriteListener<Settlement> {

    @Override
    public void afterWrite(Chunk<? extends Settlement> items) {
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Settlement> items) {
    }
}
