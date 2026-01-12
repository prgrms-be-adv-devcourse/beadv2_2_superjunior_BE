package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseParticipatedEvent;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final ProductRepository productRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> participateScript;

    @ServiceLog
    @Transactional
    public void participate(UUID groupPurchaseId, int quantity, String sellerName, String requestId) {
        // 공동 구매 조회
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        // 참여 인원 카운트 증가
        String countKey = "gp:" + groupPurchaseId + ":count";
        Long result = redisTemplate.execute(
                participateScript,
                List.of(countKey, requestId),
                String.valueOf(quantity),
                String.valueOf(groupPurchase.getMaxQuantity()),
                "3600"
        );

        if(result == -1){
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        } else if(result == -2){
            throw new CustomException(CustomErrorCode.DUPLICATE_ORDER);
        }

        // 참여 인원 증가
        groupPurchase.syncCurrentQuantity(result.intValue());
        groupPurchaseRepository.save(groupPurchase);

        // Kafka 이벤트 발행
        Product product = productRepository.findById(groupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        eventPublisher.publishEvent(
                new GroupPurchaseParticipatedEvent(groupPurchase, sellerName, product)
        );
    }
}
