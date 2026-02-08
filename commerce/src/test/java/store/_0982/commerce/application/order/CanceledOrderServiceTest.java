package store._0982.commerce.application.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderCancellationPolicy.RefundAmount;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.CanceledOrder;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CanceledOrderServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private GroupPurchaseService groupPurchaseService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CanceledOrderRepository canceledOrderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderCancellationPolicyResolver orderCancellationPolicyResolver;

    @InjectMocks
    private CanceledOrderService canceledOrderService;

    @Test
    @DisplayName("이미 취소 요청이 존재하면 아무 것도 하지 않는다")
    void cancelOrder_ignoreWhenIdempotencyKeyExists() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                orderId,
                CancelReason.CHANGE_OF_MIND,
                "duplicate",
                "idem-key"
        );

        when(canceledOrderRepository.existsByIdempotencyKey("idem-key")).thenReturn(true);

        // when
        canceledOrderService.cancelOrder(command);

        // then
        verify(canceledOrderRepository, never()).existsByOrderId(any());
        verifyNoInteractions(orderRepository, groupPurchaseService, productService,
                orderCancellationPolicyResolver, eventPublisher);
    }

    @Test
    @DisplayName("결제 완료 상태가 아니면 주문을 취소할 수 없다")
    void cancelOrder_throwsWhenOrderStatusInvalid() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                orderId,
                CancelReason.CHANGE_OF_MIND,
                "status",
                "idem-key"
        );

        Order order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        when(canceledOrderRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(canceledOrderRepository.existsByOrderId(any())).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> canceledOrderService.cancelOrder(command))
                .isInstanceOf(CustomException.class);

        verify(orderRepository).findById(orderId);
        verify(order, never()).requestCanceledAt();
        verify(canceledOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("주문이 존재하지 않으면 ORDER_NOT_FOUND 예외를 던진다")
    void cancelOrder_throwsWhenOrderNotFound() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                orderId,
                CancelReason.CHANGE_OF_MIND,
                "missing-order",
                "idem-key"
        );

        when(canceledOrderRepository.existsByIdempotencyKey("idem-key")).thenReturn(false);
        when(canceledOrderRepository.existsByOrderId(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> canceledOrderService.cancelOrder(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.ORDER_NOT_FOUND);

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(groupPurchaseService, productService, orderCancellationPolicyResolver, eventPublisher);
    }

    @Test
    @DisplayName("주문자가 아닌 회원이 취소를 시도하면 ORDER_ACCESS_DENIED 예외를 던진다")
    void cancelOrder_throwsWhenMemberMismatch() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();
        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                orderId,
                CancelReason.CHANGE_OF_MIND,
                "not-owner",
                "idem-key"
        );

        Order order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.PAYMENT_COMPLETED);
        when(order.getMemberId()).thenReturn(otherMemberId);

        when(canceledOrderRepository.existsByIdempotencyKey("idem-key")).thenReturn(false);
        when(canceledOrderRepository.existsByOrderId(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> canceledOrderService.cancelOrder(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.ORDER_ACCESS_DENIED);

        verify(orderRepository).findById(orderId);
        verify(order, never()).requestCanceledAt();
        verifyNoInteractions(groupPurchaseService, productService, orderCancellationPolicyResolver, eventPublisher);
    }

    @Test
    @DisplayName("구매자 귀책 사유면 취소 상태를 REQUESTED로 저장하고 이벤트를 발행한다")
    void cancelOrder_buyerFault_publishesEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                orderId,
                CancelReason.CHANGE_OF_MIND,
                "buyer",
                "idem-key"
        );

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getStatus()).thenReturn(OrderStatus.PAYMENT_COMPLETED);
        when(order.getMemberId()).thenReturn(memberId);
        when(order.getSellerId()).thenReturn(sellerId);
        when(order.getGroupPurchaseId()).thenReturn(groupPurchaseId);
        when(order.getPaidPrice()).thenReturn(80_000L);
        when(order.getQuantity()).thenReturn(2);
        when(order.getPaymentMethod()).thenReturn(PaymentMethod.PG);
        doNothing().when(order).requestCanceledAt();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);
        when(groupPurchase.getGroupPurchaseId()).thenReturn(groupPurchaseId);

        OrderCancellationPolicy policy = mock(OrderCancellationPolicy.class);
        RefundAmount refundAmount = new RefundAmount(70_000L, 5_000L, 5_000L);
        when(policy.calculate(order)).thenReturn(refundAmount);
        when(policy.getPolicyId()).thenReturn("CANCEL_POLICY_VOID_V1");
        when(policy.buildSnapshot(refundAmount)).thenReturn("snapshot");

        when(canceledOrderRepository.existsByIdempotencyKey("idem-key")).thenReturn(false);
        when(canceledOrderRepository.existsByOrderId(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(groupPurchaseService.findByGroupPurchase(groupPurchaseId)).thenReturn(groupPurchase);
        when(productService.findByProductName(productId)).thenReturn("product-name");
        when(orderCancellationPolicyResolver.resolve(groupPurchase, order, CancelReason.CHANGE_OF_MIND))
                .thenReturn(policy);

        ArgumentCaptor<CanceledOrder> canceledOrderCaptor = ArgumentCaptor.forClass(CanceledOrder.class);
        ArgumentCaptor<OrderCancelProcessedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelProcessedEvent.class);

        // when
        canceledOrderService.cancelOrder(command);

        // then
        verify(groupPurchaseService).decreaseQuantity(groupPurchaseId, 2);
        verify(order).requestCanceledAt();
        verify(canceledOrderRepository).save(canceledOrderCaptor.capture());
        CanceledOrder saved = canceledOrderCaptor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getMemberId()).isEqualTo(memberId);
        assertThat(saved.getSellerId()).isEqualTo(sellerId);
        assertThat(saved.getOriginalPaidAmount()).isEqualTo(80_000L);
        assertThat(saved.getCancelFeeAmount()).isEqualTo(5_000L);
        assertThat(saved.getShippingFeeAmount()).isEqualTo(5_000L);
        assertThat(saved.getRefundAmount()).isEqualTo(70_000L);
        assertThat(saved.getStatus()).isEqualTo(CancelStatus.REQUESTED);
        assertThat(saved.getReason()).isEqualTo(CancelReason.CHANGE_OF_MIND);

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderCancelProcessedEvent event = eventCaptor.getValue();
        assertThat(event.canceledOrder()).isEqualTo(saved);
        assertThat(event.productName()).isEqualTo("product-name");
    }

    @Test
    @DisplayName("판매자 귀책 사유면 PENDING 상태로 저장하고 이벤트를 발행하지 않는다")
    void cancelOrder_sellerFault_pendingWithoutEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderCancelCommand command = new OrderCancelCommand(
                memberId,
                orderId,
                CancelReason.PRODUCT_DEFECT,
                "seller",
                "idem-key"
        );

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getStatus()).thenReturn(OrderStatus.PAYMENT_COMPLETED);
        when(order.getMemberId()).thenReturn(memberId);
        when(order.getSellerId()).thenReturn(sellerId);
        when(order.getGroupPurchaseId()).thenReturn(groupPurchaseId);
        when(order.getPaidPrice()).thenReturn(120_000L);
        when(order.getQuantity()).thenReturn(4);
        when(order.getPaymentMethod()).thenReturn(PaymentMethod.POINT);
        doNothing().when(order).requestCanceledAt();

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);
        when(groupPurchase.getGroupPurchaseId()).thenReturn(groupPurchaseId);

        OrderCancellationPolicy policy = mock(OrderCancellationPolicy.class);
        RefundAmount refundAmount = new RefundAmount(120_000L, 0L, 0L);
        when(policy.calculate(order)).thenReturn(refundAmount);
        when(policy.getPolicyId()).thenReturn("CANCEL_POLICY_VOID_V1");
        when(policy.buildSnapshot(refundAmount)).thenReturn("snapshot");

        when(canceledOrderRepository.existsByIdempotencyKey("idem-key")).thenReturn(false);
        when(canceledOrderRepository.existsByOrderId(orderId)).thenReturn(false);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(groupPurchaseService.findByGroupPurchase(groupPurchaseId)).thenReturn(groupPurchase);
        when(productService.findByProductName(productId)).thenReturn("product-name");
        when(orderCancellationPolicyResolver.resolve(groupPurchase, order, CancelReason.PRODUCT_DEFECT))
                .thenReturn(policy);

        ArgumentCaptor<CanceledOrder> canceledOrderCaptor = ArgumentCaptor.forClass(CanceledOrder.class);

        // when
        canceledOrderService.cancelOrder(command);

        // then
        verify(groupPurchaseService).decreaseQuantity(groupPurchaseId, 4);
        verify(canceledOrderRepository).save(canceledOrderCaptor.capture());
        CanceledOrder saved = canceledOrderCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(CancelStatus.PENDING);
        assertThat(saved.getReason()).isEqualTo(CancelReason.PRODUCT_DEFECT);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("판매자가 보낸 승인 요청은 상태를 APPROVED 로 변경하고 이벤트를 발행한다")
    void approvePendingOrder_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CanceledOrder canceledOrder = spy(CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                sellerId,
                50_000L,
                0L,
                0L,
                50_000L,
                "policy",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "reason",
                "idem-key",
                PaymentMethod.PG
        ));

        Order order = mock(Order.class);
        when(order.getSellerId()).thenReturn(sellerId);
        when(order.getGroupPurchaseId()).thenReturn(groupPurchaseId);

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(groupPurchaseService.findByGroupPurchase(groupPurchaseId)).thenReturn(groupPurchase);
        when(productService.findByProductName(productId)).thenReturn("product-name");

        ArgumentCaptor<OrderCancelProcessedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelProcessedEvent.class);

        // when
        OrderCancelInfo info = canceledOrderService.approvePendingOrder(sellerId, orderId);

        // then
        verify(canceledOrder).markApproved();
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderCancelProcessedEvent event = eventCaptor.getValue();
        assertThat(event.canceledOrder()).isEqualTo(canceledOrder);
        assertThat(event.productName()).isEqualTo("product-name");

        assertThat(info.orderId()).isEqualTo(orderId);
        assertThat(info.status()).isEqualTo(CancelStatus.APPROVED);
        assertThat(info.refundAmount()).isEqualTo(50_000L);
    }

    @Test
    @DisplayName("취소 요청이 없으면 CANCELED_ORDER_NOT_FOUND 예외를 던진다")
    void approvePendingOrder_throwsWhenCanceledOrderMissing() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> canceledOrderService.approvePendingOrder(sellerId, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.CANCELED_ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문이 없으면 ORDER_NOT_FOUND 예외를 던진다")
    void approvePendingOrder_throwsWhenOrderMissing() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        CanceledOrder canceledOrder = mock(CanceledOrder.class);

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> canceledOrderService.approvePendingOrder(sellerId, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자가 아닌 사용자가 승인하면 NON_SELLER_ACCESS_DENIED 예외를 던진다")
    void approvePendingOrder_throwsWhenSellerMismatch() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID otherSellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CanceledOrder canceledOrder = mock(CanceledOrder.class);
        Order order = mock(Order.class);
        when(order.getSellerId()).thenReturn(otherSellerId);

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> canceledOrderService.approvePendingOrder(sellerId, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.NON_SELLER_ACCESS_DENIED);

        verify(order, never()).getGroupPurchaseId();
        verifyNoInteractions(groupPurchaseService, productService, eventPublisher);
    }
}
