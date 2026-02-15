package store._0982.commerce.integration.order;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import store._0982.commerce.application.order.CanceledOrderService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.infrastructure.kafka.publisher.OrderCanceledKafkaEventPublisher;
import store._0982.commerce.support.BaseConcurrencyTest;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.common.domain.product.Product;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCancelConcurrencyTest extends BaseConcurrencyTest {

    @Autowired
    private CanceledOrderService canceledOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CanceledOrderRepository canceledOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private OrderCanceledKafkaEventPublisher orderCanceledKafkaEventPublisher;


    private UUID sellerId;
    private UUID productId;
    private UUID groupPurchaseId;

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setUp() {
        sellerId = UUID.randomUUID();

        Product product = Product.createProduct(
                "테스트 상품",
                10000L,
                ProductCategory.FASHION,
                "동시성 테스트용 상품",
                100,
                "test.com",
                "test-image.jpg",
                UUID.randomUUID().toString(),
                sellerId
        );
        productRepository.save(product);
        productId = product.getProductId();

        GroupPurchase groupPurchase = new GroupPurchase(
                10,
                1000,
                "테스트 공동구매",
                "동시성 테스트",
                80_000L,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusDays(7),
                sellerId,
                productId,
                "test-image.jpg"
        );
        groupPurchase.open();
        groupPurchaseRepository.save(groupPurchase);
        groupPurchaseId = groupPurchase.getGroupPurchaseId();

        entityManager.clear();
    }

    @Test
    @DisplayName("동일한 공동구매의 서로 다른 주문 10개를 동시에 취소 - 모두 성공해야 함")
    void 동일_공동구매_다른_주문_10개_동시_취소_모두_성공() throws Exception {
        // Given: 10개의 주문 생성 (각 quantity: 5)
        List<Order> orders = createMultipleOrders(10, 5);

        // 공동구매 수량 증가 (10 * 5 = 50)
        for (Order order : orders) {
            GroupPurchase gp = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
            gp.increaseQuantity(order.getQuantity());
            groupPurchaseRepository.save(gp);
        }

        entityManager.clear();

        // 초기 상태 확인
        GroupPurchase gpBefore = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(gpBefore.getCurrentQuantity()).isEqualTo(50);

        List<OrderCancelCommand> commands = buildCancelCommandsForOrders(orders, CancelReason.CHANGE_OF_MIND, "다중취소");
        // Race Condition 테스트: 10개 스레드가 동시에 시작
        ConcurrencyResult result = executeRaceConditionTest(commands, "서로 다른 주문 동시 취소");

        assertThat(result.getSuccessCount()).isEqualTo(commands.size());
        assertThat(result.getFailCount()).isZero();

        entityManager.clear();

        GroupPurchase gpAfter = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(gpAfter.getCurrentQuantity()).isEqualTo(0);
        for (Order order : orders) {
            assertThat(canceledOrderRepository.findByOrderId(order.getOrderId())).isPresent();
            Order refreshed = orderRepository.findById(order.getOrderId()).orElseThrow();
            assertThat(refreshed.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Test
    @DisplayName("동일한 주문을 10번 동시 취소")
    void 동일_주문_100번_동시_취소_1번만_성공() throws Exception {
        // Given: 1개 주문 생성
        Order testOrder = createSingleOrder(UUID.randomUUID(), 3);

        // 공동구매 수량 증가
        GroupPurchase gp = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        gp.increaseQuantity(testOrder.getQuantity());
        groupPurchaseRepository.save(gp);

        entityManager.clear();

        List<OrderCancelCommand> commands = IntStream.range(0, 10)
                .mapToObj(i -> new OrderCancelCommand(
                        testOrder.getMemberId(),
                        testOrder.getOrderId(),
                        CancelReason.CHANGE_OF_MIND,
                        "중복취소",
                        "sameIdempotencyKey"
                ))
                .toList();

        // Race Condition 테스트: 100개 스레드가 동시에 시작 (멱등성 검증)
        executeRaceConditionTest(commands, "동일 주문 중복 취소");

        entityManager.clear();

        assertThat(countCanceledOrderByOrderId(testOrder.getOrderId())).isEqualTo(1L);
        Order refreshedOrder = orderRepository.findById(testOrder.getOrderId()).orElseThrow();
        assertThat(refreshedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("고부하 시나리오: 100개 주문을 동시에 취소")
    void 고부하_시나리오_50개_주문_동시_취소() throws Exception {
        // Given: 100개의 주문 생성
        int orderCount = 100;
        List<Order> orders = createMultipleOrders(orderCount, 2);

        // 공동구매 수량 증가
        for (Order order : orders) {
            GroupPurchase gp = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
            gp.increaseQuantity(order.getQuantity());
            groupPurchaseRepository.saveAndFlush(gp);
        }

        entityManager.clear();

        // 공동구매 수량 확인
        List<OrderCancelCommand> commands = buildCancelCommandsForOrders(orders, CancelReason.CHANGE_OF_MIND, "고부하");
        // 부하 테스트: 32개 스레드가 100개 작업 처리
        ConcurrencyResult result = executeLoadTest(commands, "고부하 100개 주문 취소");
        assertThat(result.getSuccessCount()).isEqualTo(commands.size());

        entityManager.clear();

        GroupPurchase gpAfter = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(gpAfter.getCurrentQuantity()).isEqualTo(0);
        for (Order order : orders) {
            assertThat(canceledOrderRepository.findByOrderId(order.getOrderId())).isPresent();
        }

    }

    List<Order> createMultipleOrders(int count, int quantityPerOrder) {
        return transactionTemplate.execute(status -> {
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                UUID memberId = UUID.randomUUID();
                Order order = createSingleOrderInternal(memberId, quantityPerOrder);
                orders.add(order);
            }
            entityManager.flush();
            entityManager.clear();
            return orders;
        });
    }

    Order createSingleOrder(UUID memberId, int quantity) {
        return transactionTemplate.execute(status -> {
            Order order = createSingleOrderInternal(memberId, quantity);
            entityManager.flush();
            entityManager.clear();
            return order;
        });
    }

    private Order createSingleOrderInternal(UUID memberId, int quantity) {
        Order order = Order.create(
                quantity,
                80_000L * quantity,
                80_000L * quantity,
                memberId,
                "서울시 강남구",
                "101호",
                "12345",
                "홍길동",
                sellerId,
                groupPurchaseId,
                "idempotency-" + UUID.randomUUID()
        );
        order.completePayment(PaymentMethod.PG);
        orderRepository.save(order);
        return order;
    }

    private List<OrderCancelCommand> buildCancelCommandsForOrders(List<Order> orders,
                                                                  CancelReason reason,
                                                                  String detailPrefix) {
        List<OrderCancelCommand> commands = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            commands.add(buildCancelCommand(order, reason, detailPrefix + "-" + i));
        }
        return commands;
    }

    private OrderCancelCommand buildCancelCommand(Order order, CancelReason reason, String detail) {
        return new OrderCancelCommand(
                order.getMemberId(),
                order.getOrderId(),
                reason,
                detail,
                "idem-" + order.getOrderId() + "-" + detail
        );
    }

    /**
     * Race Condition 테스트: 스레드 개수 = 작업 개수
     * 모든 스레드가 동시에 시작하여 경쟁
     */
    private ConcurrencyResult executeRaceConditionTest(List<OrderCancelCommand> commands,
                                                       String testName) throws InterruptedException {
        AtomicInteger index = new AtomicInteger(0);
        ConcurrencyResult result = runSynchronizedTask(commands.size(), () -> {
            int current = index.getAndIncrement();
            if (current >= commands.size()) {
                return;
            }
            canceledOrderService.cancelOrder(commands.get(current));
        });
        result.printSummary(testName);
        return result;
    }

    /**
     * 부하 테스트: 고정 스레드 < 작업 개수
     * 실제 서버 환경 시뮬레이션
     */
    private ConcurrencyResult executeLoadTest(List<OrderCancelCommand> commands,
                                              String testName) throws InterruptedException {
        AtomicInteger index = new AtomicInteger(0);
        ConcurrencyResult result = runLoadTest(32, commands.size(), () -> {
            int current = index.getAndIncrement();
            if (current >= commands.size()) {
                return;
            }
            canceledOrderService.cancelOrder(commands.get(current));
        });
        result.printSummary(testName);
        return result;
    }

    private long countCanceledOrderByOrderId(UUID orderId) {
        return entityManager.createQuery(
                        "select count(c) from CanceledOrder c where c.orderId = :orderId", Long.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }
}
