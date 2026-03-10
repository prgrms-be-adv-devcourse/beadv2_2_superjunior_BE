package store._0982.commerce.application.grouppurchase;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.support.BaseConcurrencyTest;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.Product;
import store._0982.common.domain.product.ProductCategory;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ParticipateServiceConcurrencyTest extends BaseConcurrencyTest {


    @Autowired
    private ParticipateService participateService;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseEvent> groupPurchaseKafkaTemplate;

    private GroupPurchase groupPurchase;
    private Product product;
    private UUID testSellerId;

    @BeforeEach
    void setUp(){
        testSellerId = UUID.randomUUID();
        product = createTestProduct();
        productRepository.save(product);
        groupPurchase = createTestGroupPurchase(product.getProductId(), 100); // 최대 100명
        groupPurchase.updateStatus(GroupPurchaseStatus.OPEN);
        groupPurchaseRepository.save(groupPurchase);
    }

    @Test
    @DisplayName("100명이 동시에 참여할 때 정확히 100명만 성공해야 한다")
    void participate_concurrency_shouldAllowExactlyMaxParticipants() throws InterruptedException{
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);

        runSynchronizedTask(100, () -> {
            String requestId = "request-" + index.getAndIncrement();

            try{
                participateService.participate(
                        groupPurchase.getGroupPurchaseId(),
                        1
//                        "테스트 셀러",
//                        requestId
                );

                successCount.incrementAndGet();
            } catch (CustomException e){
                if(e.getErrorCode() == CustomErrorCode.GROUP_PURCHASE_IS_REACHED){
                    failCount.incrementAndGet();
                }
            }
        });

        assertThat(successCount.get()).isEqualTo(100);
        assertThat(failCount.get()).isEqualTo(0);

        // DB 확인
        GroupPurchase updated = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).get();
        assertThat(updated.getCurrentQuantity()).isEqualTo(100);
    }


    private Product createTestProduct() {
        return Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 설명",
                100,
                null,
                null,
                "test-key",
                testSellerId
        );
    }

    private GroupPurchase createTestGroupPurchase(UUID productId, int maxQuantity){
        return new GroupPurchase(
                10, maxQuantity, "테스트 공동구매", "테스트 공동 구매 설명",
                12000L, OffsetDateTime.now().plusMinutes(5), OffsetDateTime.now().plusDays(7), testSellerId, productId, null);
    }
}
