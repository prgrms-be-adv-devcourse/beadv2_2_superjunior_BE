package store._0982.batch.grouppurchase.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.grouppurchase.config.tasklet.event.GroupPurchaseUpdatedEvent;
import store._0982.batch.grouppurchase.dto.GroupPurchaseResult;
import store._0982.commerce.infrastructure.client.order.OrderClient;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusWriter implements ItemWriter<GroupPurchaseResult> {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final OrderClient orderClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends GroupPurchaseResult> chunk) throws Exception {
        List<GroupPurchase> toUpdate = new ArrayList<>();
        
        for(GroupPurchaseResult result : chunk.getItems()){
            GroupPurchase groupPurchase = result.groupPurchase();

            if(result.success()){
                // 공동 구매 상태 변경
                groupPurchase.updateStatus(GroupPurchaseStatus.SUCCESS);

                // 주문 상태 변경
                orderClient.updateOrderStatus(
                        groupPurchase.getGroupPurchaseId(),
                        "SUCCESS"
                );

                log.info("공동 구매 성공 처리 완료 : groupPurchaseId - {}", groupPurchase.getGroupPurchaseId());
            }else {
                // 공동 구매 상태 변경
                groupPurchase.updateStatus(GroupPurchaseStatus.FAILED);

                // 주문 상태 변경
                orderClient.updateOrderStatus(
                        groupPurchase.getGroupPurchaseId(),
                        "FAILED"
                );
                log.info("공동 구매 실패 처리 완료 : groupPurchaseId - {}", groupPurchase.getGroupPurchaseId());
            }

            toUpdate.add(groupPurchase);
            eventPublisher.publishEvent(new GroupPurchaseUpdatedEvent(groupPurchase.getGroupPurchaseId()));
        }
        groupPurchaseRepository.saveAll(toUpdate);
    }
}
