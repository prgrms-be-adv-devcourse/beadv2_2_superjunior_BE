package store._0982.commerce.integration.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.order.CanceledOrderService;
import store._0982.commerce.application.order.OrderCommandService;
import store._0982.commerce.application.order.OrderExpirationService;
import store._0982.commerce.application.order.OrderSchedulerService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.commerce.support.BaseIntegrationTest;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.common.domain.product.Product;
import store._0982.common.domain.product.ProductCategory;
import store._0982.common.dto.ResponseDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 주문 생성/취소/만료 시 공동구매 참여자 수 증감 통합 테스트
 */
@DisplayName("주문 생성/취소/만료 통합 테스트")
class OrderGroupPurchaseIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private MemberClient memberClient;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private OrderSchedulerService orderSchedulerService;

    @Autowired
    private OrderExpirationService orderExpirationService;

    @Autowired
    private CanceledOrderService canceledOrderService;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UUID sellerId;
    private UUID memberId;
    private Product product;
    private GroupPurchase groupPurchase;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        // MemberClient 모킹
        when(memberClient.getMember(any()))
                .thenAnswer(invocation -> {
                    UUID requestMemberId = invocation.getArgument(0, UUID.class);
                    ProfileInfo profileInfo = new ProfileInfo(
                            requestMemberId,
                            "tester@example.com",
                            "tester",
                            OffsetDateTime.now(),
                            "ROLE_USER",
                            null,
                            "010-0000-0000"
                    );
                    return new ResponseDto<>(200, profileInfo, "ok");
                });

        // 테스트용 상품 생성
        product = Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://test.com/image.jpg",
                null,
                "test-idempotency-key-" + UUID.randomUUID(),
                sellerId
        );
        productRepository.save(product);

        // 테스트용 공동구매 생성
        groupPurchase = new GroupPurchase(
                10,  // minQuantity
                100, // maxQuantity
                "테스트 공동구매",
                "테스트 공동구매 설명",
                8000L, // discountedPrice
                OffsetDateTime.now().minusHours(1), // startDate (이미 시작)
                OffsetDateTime.now().plusDays(7),   // endDate
                sellerId,
                product.getProductId(),
                "https://test.com/image.jpg"
        );
        groupPurchase.open(); // OPEN 상태로 변경
        groupPurchaseRepository.save(groupPurchase);
    }

    @Test
    @DisplayName("주문 생성 시 공동구매 참여자 수가 증가한다")
    void createOrder_shouldIncreaseCurrentQuantity() {
        // Given
        int orderQuantity = 5;
        int initialQuantity = groupPurchase.getCurrentQuantity();

        OrderRegisterCommand command = new OrderRegisterCommand(
                orderQuantity,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );

        // When
        OrderRegisterInfo result = orderCommandService.createOrder(memberId, command);

        // Then
        GroupPurchase updated = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(updated.getCurrentQuantity()).isEqualTo(initialQuantity + orderQuantity);
        assertThat(result.orderId()).isNotNull();
    }

    @Test
    @DisplayName("주문 취소 시 공동구매 참여자 수가 감소한다")
    void cancelOrder_shouldDecreaseCurrentQuantity() {
        // Given: 주문 생성
        int orderQuantity = 5;
        OrderRegisterCommand command = new OrderRegisterCommand(
                orderQuantity,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );
        OrderRegisterInfo orderInfo = orderCommandService.createOrder(memberId, command);

        GroupPurchase afterCreate = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        int quantityAfterCreate = afterCreate.getCurrentQuantity();

        // When: 주문 취소
        Order order = orderRepository.findById(orderInfo.orderId()).orElseThrow();
        order.completePayment(PaymentMethod.PG);
        orderRepository.save(order);

        OrderCancelCommand cancelCommand = new OrderCancelCommand(
                memberId,
                orderInfo.orderId(),
                CancelReason.CHANGE_OF_MIND,
                "테스트 취소",
                UUID.randomUUID().toString()
        );
        canceledOrderService.cancelOrder(cancelCommand);

        // Then
        GroupPurchase afterCancel = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(afterCancel.getCurrentQuantity()).isEqualTo(quantityAfterCreate - orderQuantity);

        Order canceledOrder = orderRepository.findById(orderInfo.orderId()).orElseThrow();
        assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("만료된 주문 배치 처리 시 참여자 수가 감소한다")
    void expireOrders_shouldDecreaseCurrentQuantity() throws InterruptedException {
        // Given: 여러 개의 만료된 주문 생성
        int order1Qty = 3;
        int order2Qty = 5;

        // 주문 1
        OrderRegisterCommand command1 = new OrderRegisterCommand(
                order1Qty,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );
        OrderRegisterInfo order1Info = orderCommandService.createOrder(memberId, command1);

        // 주문 2
        OrderRegisterCommand command2 = new OrderRegisterCommand(
                order2Qty,
                "서울시 강남구",
                "102동 1012호",
                "12345",
                "철수",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );
        OrderRegisterInfo order2Info = orderCommandService.createOrder(UUID.randomUUID(), command2);

        GroupPurchase afterOrders = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        int quantityAfterOrders = afterOrders.getCurrentQuantity();

        // 주문을 강제로 만료 상태로 변경 (expiredAt을 과거로 설정)
        Order order1 = orderRepository.findById(order1Info.orderId()).orElseThrow();
        Order order2 = orderRepository.findById(order2Info.orderId()).orElseThrow();

        // Reflection으로 expiredAt 변경 (테스트용)
        setExpiredAt(order1, OffsetDateTime.now().minusMinutes(1));
        setExpiredAt(order2, OffsetDateTime.now().minusMinutes(1));
        orderRepository.save(order1);
        orderRepository.save(order2);

        // When: 배치 실행
        int processedCount = orderSchedulerService.processExpiredOrders();

        // Then
        assertThat(processedCount).isEqualTo(2);

        GroupPurchase afterBatch = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(afterBatch.getCurrentQuantity()).isEqualTo(quantityAfterOrders - order1Qty - order2Qty);

        Order expiredOrder1 = orderRepository.findById(order1Info.orderId()).orElseThrow();
        Order expiredOrder2 = orderRepository.findById(order2Info.orderId()).orElseThrow();
        assertThat(expiredOrder1.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        assertThat(expiredOrder2.getStatus()).isEqualTo(OrderStatus.EXPIRED);
    }

    @Test
    @DisplayName("주문 생성 → 취소 → 다시 주문 생성 시 참여자 수가 정확하다")
    void createCancelCreate_shouldMaintainCorrectQuantity() {
        // Given
        int quantity = 10;
        int initialQuantity = groupPurchase.getCurrentQuantity();

        // 1. 주문 생성
        OrderRegisterCommand command1 = new OrderRegisterCommand(
                quantity,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );
        OrderRegisterInfo orderInfo = orderCommandService.createOrder(memberId, command1);

        GroupPurchase after1 = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(after1.getCurrentQuantity()).isEqualTo(initialQuantity + quantity);

        // 2. 주문 취소
        Order order = orderRepository.findById(orderInfo.orderId()).orElseThrow();
        order.completePayment(PaymentMethod.PG);
        orderRepository.save(order);

        OrderCancelCommand cancelCommand = new OrderCancelCommand(
                memberId,
                orderInfo.orderId(),
                CancelReason.CHANGE_OF_MIND,
                "테스트 취소",
                UUID.randomUUID().toString()
        );
        canceledOrderService.cancelOrder(cancelCommand);

        GroupPurchase after2 = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(after2.getCurrentQuantity()).isEqualTo(initialQuantity);

        // 3. 다시 주문 생성
        OrderRegisterCommand command2 = new OrderRegisterCommand(
                quantity,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );
        orderCommandService.createOrder(memberId, command2);

        GroupPurchase after3 = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(after3.getCurrentQuantity()).isEqualTo(initialQuantity + quantity);
    }

    @Test
    @DisplayName("maxQuantity에 도달하면 추가 주문이 실패한다")
    void createOrder_shouldFailWhenMaxQuantityReached() {
        // Given: maxQuantity까지 주문
        int remainingQuantity = groupPurchase.getMaxQuantity() - groupPurchase.getCurrentQuantity();

        OrderRegisterCommand fillCommand = new OrderRegisterCommand(
                remainingQuantity,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );
        orderCommandService.createOrder(memberId, fillCommand);

        GroupPurchase filled = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(filled.getCurrentQuantity()).isEqualTo(groupPurchase.getMaxQuantity());
        assertThat(filled.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);

        // When & Then: 추가 주문 시도 → 실패
        OrderRegisterCommand overCommand = new OrderRegisterCommand(
                1,
                "서울시 강남구",
                "101동 101호",
                "12345",
                "철수",
                sellerId,
                groupPurchase.getGroupPurchaseId(),
                UUID.randomUUID().toString() // requestId
        );

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            orderCommandService.createOrder(UUID.randomUUID(), overCommand);
        });
    }

    @Test
    @DisplayName("동시에 10개 주문 생성 후 5개 만료 처리 시 참여자 수가 정확하다")
    void concurrentOrders_thenExpireSome() throws InterruptedException {
        // Given: 10개 주문 생성
        int orderCount = 10;
        List<UUID> orderIds = new java.util.ArrayList<>();

        for (int i = 0; i < orderCount; i++) {
            OrderRegisterCommand command = new OrderRegisterCommand(
                    1,
                    "서울시 강남구",
                    "101동 10" + i + "호",
                    "12345",
                    "홍길동" + i,
                    sellerId,
                    groupPurchase.getGroupPurchaseId(),
                    UUID.randomUUID().toString()
            );
            OrderRegisterInfo orderInfo = orderCommandService.createOrder(UUID.randomUUID(), command);
            orderIds.add(orderInfo.orderId());
        }

        GroupPurchase afterCreate = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        int quantityAfterCreate = afterCreate.getCurrentQuantity();
        assertThat(quantityAfterCreate).isEqualTo(orderCount);

        // When: 처음 5개 주문만 만료 처리
        for (int i = 0; i < 5; i++) {
            Order order = orderRepository.findById(orderIds.get(i)).orElseThrow();
            setExpiredAt(order, OffsetDateTime.now().minusMinutes(1));
            orderRepository.save(order);
        }

        int processedCount = orderSchedulerService.processExpiredOrders();

        // Then
        assertThat(processedCount).isEqualTo(5);

        GroupPurchase afterExpire = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(afterExpire.getCurrentQuantity()).isEqualTo(5);

        // 만료된 주문 확인
        for (int i = 0; i < 5; i++) {
            Order expiredOrder = orderRepository.findById(orderIds.get(i)).orElseThrow();
            assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        }

        // 만료되지 않은 주문 확인
        for (int i = 5; i < 10; i++) {
            Order pendingOrder = orderRepository.findById(orderIds.get(i)).orElseThrow();
            assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Test
    @DisplayName("동시에 여러 스레드에서 만료 처리 시 중복 처리가 발생하지 않는다")
    void concurrentExpiration_noDuplicateProcessing() throws Exception {
        // Given: 10개의 만료된 주문 생성
        int orderCount = 10;
        for (int i = 0; i < orderCount; i++) {
            OrderRegisterCommand command = new OrderRegisterCommand(
                    1,
                    "서울시 강남구",
                    "101동 10" + i + "호",
                    "12345",
                    "홍길동" + i,
                    sellerId,
                    groupPurchase.getGroupPurchaseId(),
                    UUID.randomUUID().toString()
            );
            OrderRegisterInfo orderInfo = orderCommandService.createOrder(UUID.randomUUID(), command);

            Order order = orderRepository.findById(orderInfo.orderId()).orElseThrow();
            setExpiredAt(order, OffsetDateTime.now().minusMinutes(1));
            orderRepository.save(order);
        }

        GroupPurchase beforeExpire = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        int quantityBeforeExpire = beforeExpire.getCurrentQuantity();
        assertThat(quantityBeforeExpire).isEqualTo(orderCount);

        // When: 5개 스레드가 동시에 만료 처리 실행
        int threadCount = 5;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch doneLatch = new java.util.concurrent.CountDownLatch(threadCount);

        java.util.concurrent.atomic.AtomicInteger totalProcessed = new java.util.concurrent.atomic.AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    int processed = orderSchedulerService.processExpiredOrders();
                    totalProcessed.addAndGet(processed);
                } catch (Exception e) {
                    // 예외 무시 (동시성 제어로 실패 가능)
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        // Then: 각 주문은 한 번만 처리되어야 함
        GroupPurchase afterExpire = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        assertThat(afterExpire.getCurrentQuantity()).isEqualTo(0);

        List<Order> allOrders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(groupPurchase.getGroupPurchaseId());
        assertThat(allOrders).hasSize(orderCount);
        assertThat(allOrders).allMatch(order -> order.getStatus() == OrderStatus.EXPIRED);
    }

    @Test
    @DisplayName("주문 생성 중간에 만료 처리가 실행되어도 데이터 일관성이 유지된다")
    void concurrentCreateAndExpire_dataConsistency() throws Exception {
        // Given: 초기 10개 주문 생성 (만료 대상)
        for (int i = 0; i < 10; i++) {
            OrderRegisterCommand command = new OrderRegisterCommand(
                    1,
                    "서울시 강남구",
                    "101동 10" + i + "호",
                    "12345",
                    "홍길동" + i,
                    sellerId,
                    groupPurchase.getGroupPurchaseId(),
                    UUID.randomUUID().toString()
            );
            OrderRegisterInfo orderInfo = orderCommandService.createOrder(UUID.randomUUID(), command);

            Order order = orderRepository.findById(orderInfo.orderId()).orElseThrow();
            setExpiredAt(order, OffsetDateTime.now().minusMinutes(1));
            orderRepository.save(order);
        }

        // When: 동시에 새 주문 생성 + 만료 처리
        int newOrderCount = 5;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(15);
        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch doneLatch = new java.util.concurrent.CountDownLatch(newOrderCount + 1);

        java.util.concurrent.atomic.AtomicInteger createSuccess = new java.util.concurrent.atomic.AtomicInteger();

        // 5개 스레드로 새 주문 생성
        for (int i = 0; i < newOrderCount; i++) {
            int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            1,
                            "서울시 강남구",
                            "새주문 " + index,
                            "12345",
                            "신규" + index,
                            sellerId,
                            groupPurchase.getGroupPurchaseId(),
                            "new-" + index + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(UUID.randomUUID(), command);
                    createSuccess.incrementAndGet();
                } catch (Exception e) {
                    // 실패 가능 (maxQuantity 도달 등)
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 1개 스레드로 만료 처리
        executor.submit(() -> {
            try {
                startLatch.await();
                orderSchedulerService.processExpiredOrders();
            } catch (Exception e) {
                // 예외 무시
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        doneLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        GroupPurchase finalGp = groupPurchaseRepository.findById(groupPurchase.getGroupPurchaseId()).orElseThrow();
        List<Order> allOrders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(groupPurchase.getGroupPurchaseId());

        long expiredCount = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.EXPIRED)
                .count();
        long pendingCount = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();

        // currentQuantity = 새로 생성된 주문 수 (만료된 10개는 감소됨)
        assertThat(finalGp.getCurrentQuantity()).isEqualTo(createSuccess.get());
        assertThat(expiredCount).isEqualTo(10);
        assertThat(pendingCount).isEqualTo(createSuccess.get());
    }

    /**
     * 테스트용: Reflection으로 expiredAt 필드 변경
     */
    private void setExpiredAt(Order order, OffsetDateTime expiredAt) {
        try {
            var field = Order.class.getDeclaredField("expiredAt");
            field.setAccessible(true);
            field.set(order, expiredAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set expiredAt", e);
        }
    }
}
