package store._0982.commerce.integration.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.order.CanceledOrderService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.support.BaseIntegrationTest;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.*;
import store._0982.common.domain.product.Product;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CanceledOrderServiceIntegrationTest extends BaseIntegrationTest {

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
    private ApplicationEvents applicationEvents;

    @Test
    @DisplayName("구매자 귀책 사유 취소는 REQUESTED 상태로 저장되고 이벤트를 발행한다")
    void cancelOrder_buyerFault_persistsCanceledOrder() {
        UUID sellerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        TestFixtures fixtures = prepareFixtures(sellerId, memberId, 2);
        Order order = fixtures.order;

        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                order.getOrderId(),
                CancelReason.CHANGE_OF_MIND,
                "단순 변심",
                "idem-cancel-buyer"
        );

        long beforeEvents = applicationEvents.stream(OrderCancelProcessedEvent.class).count();

        canceledOrderService.cancelOrder(command);

        CanceledOrder canceledOrder = canceledOrderRepository.findByOrderId(order.getOrderId())
                .orElseThrow();
        assertThat(canceledOrder.getStatus()).isEqualTo(CancelStatus.REQUESTED);
        assertThat(canceledOrder.getRefundAmount()).isEqualTo(order.getPaidPrice());
        assertThat(canceledOrder.getCancelFeeAmount()).isZero();
        assertThat(canceledOrder.getMemberId()).isEqualTo(memberId);

        Order canceled = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(fixtures.groupPurchaseId)
                .orElseThrow();
        assertThat(groupPurchase.getCurrentQuantity()).isZero();

        long afterEvents = applicationEvents.stream(OrderCancelProcessedEvent.class).count();
        assertThat(afterEvents - beforeEvents).isEqualTo(1);
    }

    @Test
    @DisplayName("판매자 귀책 사유 취소는 PENDING 상태로 저장되고 이벤트를 발행하지 않는다")
    void cancelOrder_sellerFault_persistsPendingWithoutEvent() {
        UUID sellerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        TestFixtures fixtures = prepareFixtures(sellerId, memberId, 1);
        Order order = fixtures.order;

        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                order.getOrderId(),
                CancelReason.PRODUCT_DEFECT,
                "상품 하자",
                "idem-cancel-seller"
        );

        long beforeEvents = applicationEvents.stream(OrderCancelProcessedEvent.class).count();

        canceledOrderService.cancelOrder(command);

        CanceledOrder canceledOrder = canceledOrderRepository.findByOrderId(order.getOrderId())
                .orElseThrow();
        assertThat(canceledOrder.getStatus()).isEqualTo(CancelStatus.PENDING);
        assertThat(canceledOrder.getReason()).isEqualTo(CancelReason.PRODUCT_DEFECT);

        long afterEvents = applicationEvents.stream(OrderCancelProcessedEvent.class).count();
        assertThat(afterEvents).isEqualTo(beforeEvents);
    }

    private TestFixtures prepareFixtures(UUID sellerId, UUID memberId, int quantity) {
        Product product = Product.createProduct(
                "상품",
                50000L,
                ProductCategory.BEAUTY,
                "설명",
                100,
                "https://example.com/product",
                null,
                "product-idem-" + UUID.randomUUID(),
                sellerId
        );
        Product savedProduct = productRepository.saveAndFlush(product);

        GroupPurchase groupPurchase = new GroupPurchase(
                1,
                100,
                "공동구매",
                "설명",
                40000L,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusDays(5),
                sellerId,
                savedProduct.getProductId(),
                null
        );
        groupPurchase.open();
        groupPurchase.increaseQuantity(quantity);
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        Order order = Order.create(
                quantity,
                50000L,
                50000L,
                memberId,
                "주소",
                "상세주소",
                "12345",
                "수령인",
                sellerId,
                savedGroupPurchase.getGroupPurchaseId(),
                "order-idem-" + UUID.randomUUID()
        );
        order.completePayment(PaymentMethod.PG);
        Order savedOrder = orderRepository.save(order);

        return new TestFixtures(savedGroupPurchase.getGroupPurchaseId(), savedOrder);
    }

    private record TestFixtures(UUID groupPurchaseId, Order order) {}
}
