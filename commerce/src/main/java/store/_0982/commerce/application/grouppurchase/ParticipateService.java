package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseParticipatedEvent;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final ProductRepository productRepository;
    private final GroupPurchaseRetryService groupPurchaseRetryService;

    private final ApplicationEventPublisher eventPublisher;

    @ServiceLog
    @Transactional
    public void participate(UUID groupPurchaseId, int quantity, String sellerName, String requestId) {
        // 공동 구매 조회
        GroupPurchase groupPurchase = groupPurchaseRetryService.participateWithRetry(groupPurchaseId, quantity);

        // Kafka 이벤트 발행
        Product product = productRepository.findById(groupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        eventPublisher.publishEvent(
                new GroupPurchaseParticipatedEvent(groupPurchase, product)
        );
    }
}
