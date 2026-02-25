package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.Product;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final GroupPurchaseCounterService groupPurchaseCounterService;
    private final TxParticipateService txParticipateService;


    @ServiceLog
    public void participate(GroupPurchase groupPurchase, Product product, int quantity) {
        int currentCount = groupPurchaseCounterService.reserve(groupPurchase, quantity);

        if (currentCount < 0) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        }

        try{
            txParticipateService.afterReserve(groupPurchase, product, currentCount);
        } catch(RuntimeException e){
            groupPurchaseCounterService.rollback(groupPurchase.getGroupPurchaseId(), quantity);
            throw e;
        }
    }

    public void rollback(UUID groupPurchaseId, int quantity){
        groupPurchaseCounterService.rollback(groupPurchaseId, quantity);
    }
}
