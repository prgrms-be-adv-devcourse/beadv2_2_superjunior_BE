package store._0982.commerce.integration.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.commerce.application.order.OrderCommandService;
import store._0982.commerce.application.order.CanceledOrderService;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
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
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Order 동시성 통합 테스트")
@Slf4j
class OrderConcurrencyIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseEvent> groupPurchaseKafkaTemplate;

    @MockitoBean
    private MemberClient memberClient;

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private CanceledOrderService canceledOrderService;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        when(memberClient.getMember(any()))
                .thenAnswer(invocation -> {
                    UUID memberId = invocation.getArgument(0, UUID.class);
                    ProfileInfo profileInfo = new ProfileInfo(
                            memberId,
                            "tester@example.com",
                            "tester",
                            OffsetDateTime.now(),
                            "ROLE_USER",
                            null,
                            "010-0000-0000"
                    );
                    return new ResponseDto<>(200, profileInfo, "ok");
                });
    }

    @Test
    @DisplayName("동시 주문 시 공동구매 참여자 수가 최대치까지만 증가한다")
    void concurrentOrderIncreaseQuantity() throws Exception {
        int maxQuantity = 1000;
        int totalRequests = 1000;
        int quantityPerOrder = 1;

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                UUID.randomUUID().toString(),
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch readyLatch = new CountDownLatch(totalRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < totalRequests; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    UUID memberId = UUID.randomUUID();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            quantityPerOrder,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "req-" + index + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(memberId, command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(savedGroupPurchase.getGroupPurchaseId());

        log.info("=== OrderConcurrencyIntegrationTest Result ===");
        log.info("successCount={}, failureCount={}, maxQuantity={}, totalRequests={}",
                successCount.get(), failureCount.get(), maxQuantity, totalRequests);
        log.info("groupPurchaseId={}, currentQuantity={}, status={}",
                updated.getGroupPurchaseId(), updated.getCurrentQuantity(), updated.getStatus());
//        orders.forEach(order -> log.info(
//                "orderId={}, orderNumber={}, quantity={}, price={}, paidPrice={}, status={}, sellerId={}, memberId={}, groupPurchaseId={}, address={}, addressDetail={}, postalCode={}, receiverName={}, idempotencyKey={}",
//                order.getOrderId(),
//                order.getOrderNumber(),
//                order.getQuantity(),
//                order.getPrice(),
//                order.getPaidPrice(),
//                order.getStatus(),
//                order.getSellerId(),
//                order.getMemberId(),
//                order.getGroupPurchaseId(),
//                order.getAddress(),
//                order.getAddressDetail(),
//                order.getPostalCode(),
//                order.getReceiverName(),
//                order.getIdempotencyKey()
//        ));

        assertThat(successCount.get()).isEqualTo(maxQuantity);
        assertThat(failureCount.get()).isEqualTo(totalRequests - maxQuantity);
        assertThat(updated.getCurrentQuantity()).isEqualTo(maxQuantity);
        assertThat(updated.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
        assertThat(orders.size()).isEqualTo(maxQuantity);
        assertThat(orders).allSatisfy(order -> {
            assertThat(order.getQuantity()).isEqualTo(quantityPerOrder);
            assertThat(order.getPrice()).isEqualTo(5000L);
            assertThat(order.getPaidPrice()).isEqualTo(5000L);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getSellerId()).isEqualTo(sellerId);
            assertThat(order.getGroupPurchaseId()).isEqualTo(savedGroupPurchase.getGroupPurchaseId());
            assertThat(order.getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getAddressDetail()).isEqualTo("상세 주소");
            assertThat(order.getPostalCode()).isEqualTo("12345");
            assertThat(order.getReceiverName()).isEqualTo("수령인");
            assertThat(order.getIdempotencyKey()).isNotBlank();
        });
    }

    @Test
    @DisplayName("max=150, 15명*15개 요청 시 10명만 성공한다")
    void concurrentOrderIncreaseQuantity_onlyTenUsersSuccess() throws Exception {
        int maxQuantity = 150;
        int totalUsers = 15;
        int quantityPerOrder = 15;

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                UUID.randomUUID().toString(),
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        ExecutorService executor = Executors.newFixedThreadPool(15);
        CountDownLatch readyLatch = new CountDownLatch(totalUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalUsers);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        List<UUID> memberIds = java.util.stream.IntStream.range(0, totalUsers)
                .mapToObj(i -> UUID.randomUUID())
                .toList();

        for (int i = 0; i < totalUsers; i++) {
            UUID memberId = memberIds.get(i);
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            quantityPerOrder,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "req-" + memberId
                    );
                    orderCommandService.createOrder(memberId, command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(savedGroupPurchase.getGroupPurchaseId());

        log.info("=== OrderConcurrencyIntegrationTest Result (150/15 users) ===");
        log.info("successCount={}, failureCount={}, maxQuantity={}, totalUsers={}, quantityPerOrder={}",
                successCount.get(), failureCount.get(), maxQuantity, totalUsers, quantityPerOrder);
        log.info("groupPurchaseId={}, currentQuantity={}, status={}",
                updated.getGroupPurchaseId(), updated.getCurrentQuantity(), updated.getStatus());
//        orders.forEach(order -> log.info(
//                "orderId={}, orderNumber={}, quantity={}, price={}, paidPrice={}, status={}, sellerId={}, memberId={}, groupPurchaseId={}, address={}, addressDetail={}, postalCode={}, receiverName={}, idempotencyKey={}",
//                order.getOrderId(),
//                order.getOrderNumber(),
//                order.getQuantity(),
//                order.getPrice(),
//                order.getPaidPrice(),
//                order.getStatus(),
//                order.getSellerId(),
//                order.getMemberId(),
//                order.getGroupPurchaseId(),
//                order.getAddress(),
//                order.getAddressDetail(),
//                order.getPostalCode(),
//                order.getReceiverName(),
//                order.getIdempotencyKey()
//        ));

        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(totalUsers - 10);
        assertThat(updated.getCurrentQuantity()).isEqualTo(maxQuantity);
        assertThat(updated.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
        assertThat(orders.size()).isEqualTo(10);
        assertThat(orders).allSatisfy(order -> {
            assertThat(order.getQuantity()).isEqualTo(quantityPerOrder);
            assertThat(order.getPrice()).isEqualTo(5000L);
            assertThat(order.getPaidPrice()).isEqualTo(5000L * quantityPerOrder);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getSellerId()).isEqualTo(sellerId);
            assertThat(order.getGroupPurchaseId()).isEqualTo(savedGroupPurchase.getGroupPurchaseId());
            assertThat(order.getAddress()).isEqualTo("서울시 강남구");
            assertThat(order.getAddressDetail()).isEqualTo("상세 주소");
            assertThat(order.getPostalCode()).isEqualTo("12345");
            assertThat(order.getReceiverName()).isEqualTo("수령인");
            assertThat(order.getIdempotencyKey()).isNotBlank();
        });
    }

    @Test
    @DisplayName("마지막 1개 남았을 때 2개 요청은 실패한다")
    void concurrentOrderIncreaseQuantity_lastOneLeft_twoRequested() throws Exception {
        int maxQuantity = 10;
        int preFilled = 9;

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                "test-key-lastOneLeft",
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        for (int i = 0; i < preFilled; i++) {
            UUID memberId = UUID.randomUUID();
            OrderRegisterCommand command = new OrderRegisterCommand(
                    1,
                    "서울시 강남구",
                    "상세 주소",
                    "12345",
                    "수령인",
                    sellerId,
                    savedGroupPurchase.getGroupPurchaseId(),
                    "prefill-" + i + "-" + UUID.randomUUID()
            );
            orderCommandService.createOrder(memberId, command);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                OrderRegisterCommand command = new OrderRegisterCommand(
                        1,
                        "서울시 강남구",
                        "상세 주소",
                        "12345",
                        "수령인",
                        sellerId,
                        savedGroupPurchase.getGroupPurchaseId(),
                        "req-1-" + UUID.randomUUID()
                );
                orderCommandService.createOrder(UUID.randomUUID(), command);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                OrderRegisterCommand command = new OrderRegisterCommand(
                        2,
                        "서울시 강남구",
                        "상세 주소",
                        "12345",
                        "수령인",
                        sellerId,
                        savedGroupPurchase.getGroupPurchaseId(),
                        "req-2-" + UUID.randomUUID()
                );
                orderCommandService.createOrder(UUID.randomUUID(), command);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(savedGroupPurchase.getGroupPurchaseId());

        log.info("=== OrderConcurrencyIntegrationTest Result (last one left) ===");
        log.info("successCount={}, failureCount={}, maxQuantity={}, preFilled={}",
                successCount.get(), failureCount.get(), maxQuantity, preFilled);
        log.info("groupPurchaseId={}, currentQuantity={}, status={}",
                updated.getGroupPurchaseId(), updated.getCurrentQuantity(), updated.getStatus());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
        assertThat(updated.getCurrentQuantity()).isEqualTo(maxQuantity);
        assertThat(updated.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
        assertThat(orders.size()).isEqualTo(maxQuantity);
        assertThat(orders.stream().filter(o -> o.getQuantity() == 2).count()).isZero();
    }

    @Test
    @DisplayName("동시에 참여와 취소가 발생해도 참여자 수가 일관되게 유지된다")
    void concurrentParticipateAndCancel_consistentQuantity() throws Exception {
        int maxQuantity = 50;
        int preFilled = 10;
        int createThreads = 10;
        int cancelThreads = 10;

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                UUID.randomUUID().toString(),
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        // prefill 10 (PAYMENT_COMPLETED) so they can be canceled
        List<Order> prefilledOrders = java.util.stream.IntStream.range(0, preFilled)
                .mapToObj(i -> {
                    UUID memberId = UUID.randomUUID();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            1,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "prefill-" + i + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(memberId, command);
                    Order order = orderRepository.findByIdempotenceKey(command.requestId())
                            .orElseThrow();
                    order.completePayment(PaymentMethod.PG);
                    return orderRepository.save(order);
                })
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch readyLatch = new CountDownLatch(createThreads + cancelThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(createThreads + cancelThreads);

        AtomicInteger createSuccess = new AtomicInteger();
        AtomicInteger createFail = new AtomicInteger();
        AtomicInteger cancelSuccess = new AtomicInteger();
        AtomicInteger cancelFail = new AtomicInteger();

        for (int i = 0; i < createThreads; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    UUID memberId = UUID.randomUUID();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            1,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "create-" + index + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(memberId, command);
                    createSuccess.incrementAndGet();
                } catch (Exception e) {
                    createFail.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        for (int i = 0; i < cancelThreads; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    Order order = prefilledOrders.get(index);
                    OrderCancelCommand cancelCommand = new OrderCancelCommand(
                            order.getMemberId(),
                            order.getOrderId(),
                            CancelReason.CHANGE_OF_MIND,
                            "테스트 취소",
                            "cancel-" + index + "-" + UUID.randomUUID()
                    );
                    canceledOrderService.cancelOrder(cancelCommand);
                    cancelSuccess.incrementAndGet();
                } catch (Exception e) {
                    cancelFail.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(savedGroupPurchase.getGroupPurchaseId());

        log.info("=== OrderConcurrencyIntegrationTest Result (participate + cancel) ===");
        log.info("createSuccess={}, createFail={}, cancelSuccess={}, cancelFail={}",
                createSuccess.get(), createFail.get(), cancelSuccess.get(), cancelFail.get());
        log.info("groupPurchaseId={}, currentQuantity={}, status={}",
                updated.getGroupPurchaseId(), updated.getCurrentQuantity(), updated.getStatus());

        int expectedCurrentQuantity = preFilled + createSuccess.get() - cancelSuccess.get();
        assertThat(updated.getCurrentQuantity()).isEqualTo(expectedCurrentQuantity);
        assertThat(orders.size()).isEqualTo(preFilled + createSuccess.get());
    }

    @Test
    @DisplayName("같은 주문을 동시에 여러 번 취소 시도하면 1번만 성공한다")
    void concurrentCancelSameOrder_onlyOneSuccess() throws Exception {
        int maxQuantity = 50;
        int cancelAttempts = 5;

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                UUID.randomUUID().toString(),
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        // 1개 주문 생성 후 PAYMENT_COMPLETED 상태로 변경
        UUID memberId = UUID.randomUUID();
        OrderRegisterCommand command = new OrderRegisterCommand(
                1,
                "서울시 강남구",
                "상세 주소",
                "12345",
                "수령인",
                sellerId,
                savedGroupPurchase.getGroupPurchaseId(),
                "target-order-" + UUID.randomUUID()
        );
        orderCommandService.createOrder(memberId, command);
        Order targetOrder = orderRepository.findByIdempotenceKey(command.requestId())
                .orElseThrow();
        targetOrder.completePayment(PaymentMethod.PG);
        orderRepository.save(targetOrder);

        ExecutorService executor = Executors.newFixedThreadPool(cancelAttempts);
        CountDownLatch readyLatch = new CountDownLatch(cancelAttempts);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(cancelAttempts);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // 5개 스레드가 동시에 같은 주문 취소 시도
        for (int i = 0; i < cancelAttempts; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    OrderCancelCommand cancelCommand = new OrderCancelCommand(
                            memberId,
                            targetOrder.getOrderId(),
                            CancelReason.CHANGE_OF_MIND,
                            "동시 취소 테스트",
                            "cancel-" + index + "-" + UUID.randomUUID()
                    );
                    canceledOrderService.cancelOrder(cancelCommand);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        Order updatedOrder = orderRepository.findById(targetOrder.getOrderId())
                .orElseThrow();

        log.info("=== OrderConcurrencyIntegrationTest Result (same order cancel) ===");
        log.info("successCount={}, failureCount={}, cancelAttempts={}",
                successCount.get(), failureCount.get(), cancelAttempts);
        log.info("orderId={}, status={}, groupPurchaseId={}, currentQuantity={}",
                updatedOrder.getOrderId(), updatedOrder.getStatus(),
                updated.getGroupPurchaseId(), updated.getCurrentQuantity());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(cancelAttempts - 1);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(updated.getCurrentQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("참여자 수가 다 차기 직전 참여 시 GROUP_PURCHASE_IS_REACHED 에러 발생")
    void concurrentOrder_nearMaxQuantity_throwsGroupPurchaseIsReachedError() throws Exception {
        int maxQuantity = 10;
        int preFilled = 9;  // 1개 남은 상태

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                "test-key-near-max",
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        // 9개 미리 채우기
        for (int i = 0; i < preFilled; i++) {
            UUID memberId = UUID.randomUUID();
            OrderRegisterCommand command = new OrderRegisterCommand(
                    1,
                    "서울시 강남구",
                    "상세 주소",
                    "12345",
                    "수령인",
                    sellerId,
                    savedGroupPurchase.getGroupPurchaseId(),
                    "prefill-" + i + "-" + UUID.randomUUID()
            );
            orderCommandService.createOrder(memberId, command);
        }

        // 2명이 동시에 주문 시도
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger groupPurchaseReachedErrorCount = new AtomicInteger();
        AtomicInteger otherErrorCount = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    UUID memberId = UUID.randomUUID();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            1,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "concurrent-" + index + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(memberId, command);
                    successCount.incrementAndGet();
                } catch (store._0982.common.exception.CustomException e) {
                    if (e.getErrorCode() == store._0982.commerce.exception.CustomErrorCode.GROUP_PURCHASE_IS_REACHED) {
                        groupPurchaseReachedErrorCount.incrementAndGet();
                        log.info("GROUP_PURCHASE_IS_REACHED 에러 발생 (예상된 동작)");
                    } else {
                        otherErrorCount.incrementAndGet();
                        log.error("예상치 못한 CustomException: {}", e.getErrorCode(), e);
                    }
                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    log.error("예상치 못한 Exception", e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(savedGroupPurchase.getGroupPurchaseId());

        log.info("=== OrderConcurrencyIntegrationTest Result (GROUP_PURCHASE_IS_REACHED 에러 테스트) ===");
        log.info("successCount={}, groupPurchaseReachedErrorCount={}, otherErrorCount={}",
                successCount.get(), groupPurchaseReachedErrorCount.get(), otherErrorCount.get());
        log.info("maxQuantity={}, preFilled={}, currentQuantity={}, status={}",
                maxQuantity, preFilled, updated.getCurrentQuantity(), updated.getStatus());

        // 검증
        assertThat(successCount.get()).isEqualTo(1);  // 1명만 성공
        assertThat(groupPurchaseReachedErrorCount.get()).isEqualTo(1);  // 1명은 GROUP_PURCHASE_IS_REACHED 에러
        assertThat(otherErrorCount.get()).isZero();  // 다른 에러는 없어야 함
        assertThat(updated.getCurrentQuantity()).isEqualTo(maxQuantity);  // 최대치까지만 증가
        assertThat(updated.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
        assertThat(orders.size()).isEqualTo(maxQuantity);  // 총 10개 주문만 존재
    }

    @Test
    @DisplayName("최대치에 가까울 때 참여와 취소가 동시 발생")
    void concurrentParticipateAndCancel_nearMaxQuantity() throws Exception {
        int maxQuantity = 50;
        int preFilled = 48;
        int participateAttempts = 5;
        int cancelAttempts = 2;

        UUID sellerId = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                null,
                UUID.randomUUID().toString(),
                sellerId
        ));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                maxQuantity,
                "테스트 공동구매",
                "설명",
                5000L,
                now.minusDays(1),
                now.plusDays(3),
                sellerId,
                product.getProductId(),
                null
        );
        groupPurchase.open();
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        // preFilled=48개 주문 생성 (처음 2개는 PAYMENT_COMPLETED, 나머지는 PENDING)
        List<Order> cancelableOrders = java.util.stream.IntStream.range(0, preFilled)
                .mapToObj(i -> {
                    UUID memberId = UUID.randomUUID();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            1,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "prefill-" + i + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(memberId, command);
                    Order order = orderRepository.findByIdempotenceKey(command.requestId())
                            .orElseThrow();

                    // 처음 2개만 결제 완료 상태로 변경 (취소 가능)
                    if (i < cancelAttempts) {
                        order.completePayment(PaymentMethod.PG);
                        return orderRepository.save(order);
                    }
                    return order;
                })
                .filter(order -> order.getStatus() == OrderStatus.PAYMENT_COMPLETED)
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch readyLatch = new CountDownLatch(participateAttempts + cancelAttempts);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(participateAttempts + cancelAttempts);

        AtomicInteger participateSuccess = new AtomicInteger();
        AtomicInteger participateFail = new AtomicInteger();
        AtomicInteger cancelSuccess = new AtomicInteger();
        AtomicInteger cancelFail = new AtomicInteger();

        // 5개 스레드로 참여 시도
        for (int i = 0; i < participateAttempts; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    UUID memberId = UUID.randomUUID();
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            1,
                            "서울시 강남구",
                            "상세 주소",
                            "12345",
                            "수령인",
                            sellerId,
                            savedGroupPurchase.getGroupPurchaseId(),
                            "participate-" + index + "-" + UUID.randomUUID()
                    );
                    orderCommandService.createOrder(memberId, command);
                    participateSuccess.incrementAndGet();
                } catch (Exception e) {
                    participateFail.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 2개 스레드로 취소 시도
        for (int i = 0; i < cancelAttempts; i++) {
            int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    Order order = cancelableOrders.get(index);
                    OrderCancelCommand cancelCommand = new OrderCancelCommand(
                            order.getMemberId(),
                            order.getOrderId(),
                            CancelReason.CHANGE_OF_MIND,
                            "최대치 근처 취소 테스트",
                            "cancel-near-max-" + index + "-" + UUID.randomUUID()
                    );
                    canceledOrderService.cancelOrder(cancelCommand);
                    cancelSuccess.incrementAndGet();
                } catch (Exception e) {
                    cancelFail.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(10, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        GroupPurchase updated = groupPurchaseRepository.findById(savedGroupPurchase.getGroupPurchaseId())
                .orElseThrow();
        List<Order> allOrders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(savedGroupPurchase.getGroupPurchaseId());
        long canceledCount = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                .count();

        log.info("=== OrderConcurrencyIntegrationTest Result (near max quantity) ===");
        log.info("participateSuccess={}, participateFail={}, cancelSuccess={}, cancelFail={}",
                participateSuccess.get(), participateFail.get(), cancelSuccess.get(), cancelFail.get());
        log.info("maxQuantity={}, preFilled={}, currentQuantity={}, status={}",
                maxQuantity, preFilled, updated.getCurrentQuantity(), updated.getStatus());
        log.info("allOrders={}, canceledCount={}", allOrders.size(), canceledCount);

        // 예상 결과 계산
        // preFilled=48, 취소 성공=cancelSuccess, 참여 성공=participateSuccess
        // currentQuantity = 48 - cancelSuccess + participateSuccess (단, 최대 50)
        int expectedCurrentQuantity = preFilled - cancelSuccess.get() + participateSuccess.get();

        assertThat(updated.getCurrentQuantity()).isEqualTo(expectedCurrentQuantity);
        assertThat(updated.getCurrentQuantity()).isLessThanOrEqualTo(maxQuantity);
        assertThat(canceledCount).isEqualTo(cancelSuccess.get());

        // 48 + 2 = 50이므로, 참여는 최대 2개만 성공해야 함
        // 취소가 2개 성공하면 참여는 최대 4개 성공 가능
        // 취소가 동시에 일어나면 참여 가능 개수가 달라짐
        assertThat(participateSuccess.get()).isLessThanOrEqualTo(maxQuantity - preFilled + cancelSuccess.get());
    }
}
