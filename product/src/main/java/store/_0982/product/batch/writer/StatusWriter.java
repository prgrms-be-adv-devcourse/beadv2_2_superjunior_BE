package store._0982.product.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.common.dto.ResponseDto;
import store._0982.product.client.OrderClient;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;
import store._0982.product.domain.GroupPurchaseStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusWriter implements ItemWriter<GroupPurchase> {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final OrderClient orderClient;

    @Override
    public void write(Chunk<? extends GroupPurchase> chunk) throws Exception {
        List<GroupPurchase> items = new ArrayList<>(chunk.getItems());
        for(GroupPurchase groupPurchase : items){

            try{
                if(groupPurchase.getStatus() == GroupPurchaseStatus.SUCCESS){
                    ResponseDto<Void> response = orderClient.updateOrderStatus(
                            groupPurchase.getGroupPurchaseId(),
                            "SUCCESS"
                    );
                    log.debug("주문 상태 성공으로 변경");
                }else{
                    ResponseDto<Void> response = orderClient.updateOrderStatus(
                            groupPurchase.getGroupPurchaseId(),
                            "FAILED"
                    );
                    log.debug("주문 상태 실패로 변경");
                }
            }catch(Exception e){
                log.error("주문 상태 변경 실패: groupPurchaseId={}", groupPurchase.getGroupPurchaseId(), e);
            }
        }
    }
}
