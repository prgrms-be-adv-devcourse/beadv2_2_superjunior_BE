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
import store._0982.commerce.application.order.OrderService;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

public class ParticipateServiceConcurrencyTest extends BaseConcurrencyTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    @Autowired
    private OrderService orderService;

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
    @DisplayName("공동구매 주문을 100명 요청 시 100명이 정상적으로 성공한다")
    void participate_concurrency_shouldAllowExactlyMaxParticipants() throws InterruptedException{
        int totalRequest = 100;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);

        initializeConcurrencyContext(totalRequest);

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
                }else{
                    System.err.println("Unexpected CustomException: " + e.getErrorCode() + " - " + e.getMessage());
                    exceptionCount.incrementAndGet();
                }
            } catch(Exception e){
                System.err.println("Unexpected Exception: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                exceptionCount.incrementAndGet();
            }
        });

        assertThat(successCount.get()).isEqualTo(100);
        assertThat(failCount.get()).isEqualTo(0);
        assertThat(exceptionCount.get()).isEqualTo(0);

        // Redis 카운트 확인
        String countKey = "gp:" + groupPurchase.getGroupPurchaseId() + ":count";
        String redisCount = redisTemplate.opsForValue().get(countKey);
        assertThat(redisCount).isEqualTo("100");

        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MICROSECONDS)
                .untilAsserted(() -> {
                    GroupPurchase updated = groupPurchaseRepository
                            .findById(groupPurchase.getGroupPurchaseId())
                            .orElseThrow();
                    assertThat(updated.getCurrentQuantity()).isEqualTo(100);
                });
    }

    @Test
    @DisplayName("DB 비동기로 업데이트 확인")
    void participate_sync_DB (){
        String requestId = UUID.randomUUID().toString();
        String countKey = "gp:" + groupPurchase.getGroupPurchaseId() + ":count";

        participateService.participate(
                groupPurchase.getGroupPurchaseId(),
                5,
                "테스트 셀러",
                requestId
        );

        String redisCount = redisTemplate.opsForValue().get(countKey);
        assertThat(redisCount).isEqualTo("5");

        await().atMost(3, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MICROSECONDS)
                .untilAsserted(() -> {
                    GroupPurchase updated = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId())
                            .orElseThrow();
                    assertThat(updated.getCurrentQuantity()).isEqualTo(5);
                });

    }

    @Test
    @DisplayName("공동구매 정원(100명) 초과 요청 시 초과 인원은 실패한다")
    void participate_concurrency_shouldRejectOverMaxParticipants() throws InterruptedException {

        int totalRequest = 120;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);

        initializeConcurrencyContext(totalRequest);

        runSynchronizedTask(() -> {
            String requestId = "request-" + index.getAndIncrement();

            try {
                participateService.participate(
                        groupPurchase.getGroupPurchaseId(),
                        1,
                        "테스트 셀러",
                        requestId
                );
                successCount.incrementAndGet();

            } catch (CustomException e) {
                if (e.getErrorCode() == CustomErrorCode.GROUP_PURCHASE_IS_REACHED) {
                    failCount.incrementAndGet();
                } else {
                    System.err.println("Unexpected CustomException: " + e.getErrorCode());
                    exceptionCount.incrementAndGet();
                }
            } catch (Exception e) {
                System.err.println("Unexpected Exception: " + e.getMessage());
                exceptionCount.incrementAndGet();
            }
        });

        System.out.println("Success: " + successCount.get());
        System.out.println("Fail: " + failCount.get());
        System.out.println("Exception: " + exceptionCount.get());
        System.out.println("Total: " + (successCount.get() + failCount.get() + exceptionCount.get()));

        assertThat(successCount.get()).isEqualTo(100);
        assertThat(failCount.get()).isEqualTo(20);
        assertThat(exceptionCount.get()).isEqualTo(0);

        String countKey = "gp:" + groupPurchase.getGroupPurchaseId() + ":count";
        String redisCount = redisTemplate.opsForValue().get(countKey);

        assertThat(redisCount).isEqualTo("100");

        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MICROSECONDS)
                .untilAsserted(() -> {
                    GroupPurchase updated = groupPurchaseRepository
                            .findById(groupPurchase.getGroupPurchaseId())
                            .orElseThrow();

                    assertThat(updated.getCurrentQuantity()).isEqualTo(100);
                });
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
