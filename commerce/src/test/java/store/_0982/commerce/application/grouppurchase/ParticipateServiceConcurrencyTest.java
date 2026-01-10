package store._0982.commerce.application.grouppurchase;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.support.BaseConcurrencyTest;
import store._0982.common.exception.CustomException;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ParticipateServiceConcurrencyTest extends BaseConcurrencyTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ParticipateService participateService;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private GroupPurchase groupPurchase;
    private Product product;
    private UUID testSellerId;

    @Override
    protected int getDefaultThreadCount() {
        return 100;  // 100개 스레드
    }

    @BeforeEach
    void setUp(){
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
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

        runSynchronizedTask(() -> {
            String requestId = "request-" + index.getAndIncrement();

            try{
                participateService.participate(
                        groupPurchase.getGroupPurchaseId(),
                        1,
                        "테스트 셀러",
                        requestId
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

        // Redis 카운트 확인
        String countKey = "gp:" + groupPurchase.getGroupPurchaseId() + ":count";
        String redisCount = redisTemplate.opsForValue().get(countKey);
        assertThat(redisCount).isEqualTo("100");

        // DB 확인
        GroupPurchase updated = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).get();
        assertThat(updated.getCurrentQuantity()).isEqualTo(100);
    }


    private Product createTestProduct() {
        return new Product(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 설명",
                100,
                null,
                testSellerId
        );
    }

    private GroupPurchase createTestGroupPurchase(UUID productId, int maxQuantity){
        return new GroupPurchase(
                10,maxQuantity,"테스트 공동구매", "테스트 공동 구매 설명",
                12000L, OffsetDateTime.now().plusMinutes(5), OffsetDateTime.now().plusDays(7), testSellerId, productId);
    }
}
