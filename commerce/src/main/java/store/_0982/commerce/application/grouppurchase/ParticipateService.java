package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.Product;
import store._0982.common.log.ServiceLog;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final ProductRepository productRepository;
    private final GroupPurchaseQuantityService groupPurchaseQuantityService;
    private final TxParticipateService txParticipateService;

    private final ApplicationEventPublisher eventPublisher;

    @ServiceLog
    @Transactional
    public void participate(GroupPurchase groupPurchase, Product product, int quantity) {

        // 공동 구매 조회
        GroupPurchase increased = groupPurchaseQuantityService.increaseQuantity(groupPurchase.getGroupPurchaseId(), quantity);

        int currentCount = increased.getCurrentQuantity();

        try{
            txParticipateService.afterReserve(groupPurchase, product, currentCount);
        } catch(RuntimeException e){
            groupPurchaseQuantityService.decreaseQuantity(groupPurchase.getGroupPurchaseId(), quantity);
            throw e;
        }
    }
}
