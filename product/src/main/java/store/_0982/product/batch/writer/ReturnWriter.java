package store._0982.product.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.product.batch.dto.GroupPurchaseResult;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class ReturnWriter implements ItemWriter<GroupPurchaseResult> {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProducerFactory<String, GroupPurchaseEvent> groupPurchaseProducerFactory;

    @Override
    public void write(Chunk<? extends GroupPurchaseResult> chunk) throws Exception {
        long successCount = 0;
        long failCount = 0;

        List<GroupPurchase> toUpdate = new ArrayList<>();
        for (GroupPurchaseResult result : chunk.getItems()) {
            if(!result.success()){
                failCount ++;
            }

            GroupPurchase groupPurchase = result.groupPurchase();
            groupPurchase.markAsReturned();
            toUpdate.add(groupPurchase);
            successCount++;
        }

        groupPurchaseRepository.saveAll(toUpdate);
        log.info("환불 처리 완료 : 성공 = {}, 실패 = {}", successCount, failCount);
    }
}
