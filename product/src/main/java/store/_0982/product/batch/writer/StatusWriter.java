package store._0982.product.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.common.dto.ResponseDto;
import store._0982.product.batch.dto.GroupPurchaseResult;
import store._0982.product.client.OrderClient;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;
import store._0982.product.domain.GroupPurchaseStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusWriter implements ItemWriter<GroupPurchaseResult> {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final OrderClient orderClient;

    @Override
    public void write(Chunk<? extends GroupPurchaseResult> chunk) throws Exception {

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
        }
    }
}
