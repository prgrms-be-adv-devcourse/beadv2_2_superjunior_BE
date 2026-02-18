package store._0982.commerce.integration.order;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import store._0982.commerce.application.order.CanceledOrderService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.infrastructure.kafka.publisher.OrderCanceledKafkaEventPublisher;
import store._0982.commerce.support.BaseConcurrencyTest;
import store._0982.commerce.support.concurrency.ConcurrencyResult;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.common.domain.product.Product;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public abstract class AbstractOrderCancelConcurrencySupport extends BaseConcurrencyTest {

    @Autowired
    protected CanceledOrderService canceledOrderService;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CanceledOrderRepository canceledOrderRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @MockitoBean
    protected OrderCanceledKafkaEventPublisher orderCanceledKafkaEventPublisher;

    protected UUID sellerId;
    protected UUID productId;
    protected UUID groupPurchaseId;

    @BeforeEach
    void setUpData() {
        transactionTemplate.execute(status -> {
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

            entityManager.flush();
            entityManager.clear();
            return null;
        });
    }

    protected List<Order> createMultipleOrders(int count, int quantityPerOrder) {
        return transactionTemplate.execute(status -> {
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                UUID memberId = UUID.randomUUID();
                orders.add(createSingleOrderInternal(memberId, quantityPerOrder));
            }
            entityManager.flush();
            entityManager.clear();
            return orders;
        });
    }

    protected Order createSingleOrder(UUID memberId, int quantity) {
        return transactionTemplate.execute(status -> {
            Order order = createSingleOrderInternal(memberId, quantity);
            entityManager.flush();
            entityManager.clear();
            return order;
        });
    }

    protected Order createSingleOrderInternal(UUID memberId, int quantity) {
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

    protected void increaseGroupPurchaseQuantityByOrders(List<Order> orders) {
        transactionTemplate.execute(status -> {
            GroupPurchase gp = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
            for (Order order : orders) {
                gp.increaseQuantity(order.getQuantity());
            }
            groupPurchaseRepository.saveAndFlush(gp);
            entityManager.clear();
            return null;
        });
    }

    protected List<OrderCancelCommand> buildCancelCommandsForOrders(List<Order> orders) {
        List<OrderCancelCommand> commands = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            commands.add(new OrderCancelCommand(
                    order.getMemberId(),
                    order.getOrderId(),
                    CancelReason.CHANGE_OF_MIND,
                    "detailReason",
                    "idem-" + order.getOrderId() + "-" + UUID.randomUUID() + "-" + i
            ));
        }
        return commands;
    }

    protected List<OrderCancelCommand> buildSameOrderCommands(Order order, int times) {
        return IntStream.range(0, times)
                .mapToObj(i -> new OrderCancelCommand(
                        order.getMemberId(),
                        order.getOrderId(),
                        CancelReason.CHANGE_OF_MIND,
                        "중복취소-" + i,
                        "sameIdempotencyKey"
                ))
                .toList();
    }

    protected ConcurrencyResult runRace(List<OrderCancelCommand> commands) throws InterruptedException {
        AtomicInteger index = new AtomicInteger(0);
        return runSynchronizedTask(commands.size(), () -> {
            int current = index.getAndIncrement();
            if (current >= commands.size()) return;
            canceledOrderService.cancelOrder(commands.get(current));
        });
    }

    protected ConcurrencyResult runLoad(int threads, List<OrderCancelCommand> commands) throws InterruptedException {
        AtomicInteger index = new AtomicInteger(0);
        return runLoadTest(threads, commands.size(), () -> {
            int current = index.getAndIncrement();
            if (current >= commands.size()) return;
            canceledOrderService.cancelOrder(commands.get(current));
        });
    }

    protected long countCanceledOrderByOrderId(UUID orderId) {
        return entityManager.createQuery(
                        "select count(c) from CanceledOrder c where c.orderId = :orderId", Long.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }
}
