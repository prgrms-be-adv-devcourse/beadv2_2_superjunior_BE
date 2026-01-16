package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.settlement.Settlement;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryFailedSettlementWriter implements ItemWriter<Settlement> {

    @Transactional
    @Override
    public void write(Chunk<? extends Settlement> chunk) {

    }
}
