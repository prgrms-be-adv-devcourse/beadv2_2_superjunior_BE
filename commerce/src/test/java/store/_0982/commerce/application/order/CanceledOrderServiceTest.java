package store._0982.commerce.application.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.commerce.application.grouppurchase.GroupPurchaseQuantityService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.commerce.domain.order.OrderCancellationPolicy.RefundAmount;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.*;
import store._0982.common.exception.CustomException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
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
    private GroupPurchaseQuantityService groupPurchaseQuantityService;

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
        verify(groupPurchaseQuantityService).decreaseQuantity(groupPurchaseId, 2);
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
        verify(groupPurchaseQuantityService).decreaseQuantity(groupPurchaseId, 4);
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

    @Test
    @DisplayName("판매자가 거부하면 상태를 REJECTED 로 변경한다")
    void rejectPendingOrder_success() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CanceledOrder canceledOrder = spy(CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                sellerId,
                30_000L,
                0L,
                0L,
                30_000L,
                "policy",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "reason",
                "idem-key",
                PaymentMethod.POINT
        ));

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(canceledOrder));

        // when
        OrderCancelInfo info = canceledOrderService.rejectPendingOrder(sellerId, orderId);

        // then
        verify(canceledOrder).markRejected();
        assertThat(info.orderId()).isEqualTo(orderId);
        assertThat(info.status()).isEqualTo(CancelStatus.REJECTED);
        assertThat(info.refundAmount()).isEqualTo(30_000L);
    }

    @Test
    @DisplayName("거부 대상 취소 요청이 없으면 예외를 던진다")
    void rejectPendingOrder_throwsWhenCanceledOrderMissing() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> canceledOrderService.rejectPendingOrder(sellerId, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.CANCELED_ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자가 아니면 거부할 수 없다")
    void rejectPendingOrder_throwsWhenSellerMismatch() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID otherSellerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                otherSellerId,
                30_000L,
                0L,
                0L,
                30_000L,
                "policy",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "reason",
                "idem-key",
                PaymentMethod.PG
        );

        when(canceledOrderRepository.findByOrderId(orderId)).thenReturn(Optional.of(canceledOrder));

        // when & then
        assertThatThrownBy(() -> canceledOrderService.rejectPendingOrder(sellerId, orderId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CustomErrorCode.NON_SELLER_ACCESS_DENIED);
    }

    @Test
    @DisplayName("재시도 조회는 REQUESTED/APPROVED 상태와 15분 이전을 기준으로 수행한다")
    void retryCancelOrder_filtersStatusesAndTime() {
        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of());

        ArgumentCaptor<List<CancelStatus>> statusCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OffsetDateTime> timeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        canceledOrderService.retryCancelOrder();

        verify(canceledOrderRepository)
                .findAllByStatusInAndCanceledAtBefore(statusCaptor.capture(), timeCaptor.capture());

        assertThat(statusCaptor.getValue())
                .containsExactlyInAnyOrder(CancelStatus.REQUESTED, CancelStatus.APPROVED);

        long minutes = Duration.between(timeCaptor.getValue(), OffsetDateTime.now()).toMinutes();
        assertThat(minutes).isGreaterThanOrEqualTo(15);
    }

    @Test
    @DisplayName("재시도 대상이 없으면 이벤트를 발행하지 않는다")
    void retryCancelOrder_noPendingOrders() {
        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of());

        canceledOrderService.retryCancelOrder();

        verify(orderRepository, never()).findById(any());
        verify(eventPublisher, never()).publishEvent(any());
        verifyNoInteractions(orderCancellationPolicyResolver, groupPurchaseService, productService);
    }

    @Test
    @DisplayName("주문을 찾을 수 없으면 해당 취소 건을 건너뛴다")
    void retryCancelOrder_skipWhenOrderMissing() {
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                sellerId,
                10_000L,
                0L,
                0L,
                10_000L,
                "policy",
                "snapshot",
                CancelStatus.REQUESTED,
                CancelReason.CHANGE_OF_MIND,
                "reason",
                "idem-key",
                PaymentMethod.PG
        );

        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        canceledOrderService.retryCancelOrder();

        verify(orderRepository).findById(orderId);
        verify(orderCancellationPolicyResolver, never()).resolveByPolicyId(anyString());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("정책을 찾지 못하면 이벤트를 발행하지 않는다")
    void retryCancelOrder_skipWhenPolicyMissing() {
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                sellerId,
                15_000L,
                0L,
                0L,
                15_000L,
                "policy",
                "snapshot",
                CancelStatus.APPROVED,
                CancelReason.CHANGE_OF_MIND,
                "reason",
                "idem-key",
                PaymentMethod.PG
        );

        Order order = mock(Order.class);

        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderCancellationPolicyResolver.resolveByPolicyId("policy")).thenReturn(null);

        canceledOrderService.retryCancelOrder();

        verify(orderRepository).findById(orderId);
        verify(orderCancellationPolicyResolver).resolveByPolicyId("policy");
        verify(groupPurchaseService, never()).findByGroupPurchase(any());
        verify(productService, never()).findByProductName(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("유효한 취소 건은 재시도 작업에서 이벤트를 발행한다")
    void retryCancelOrder_publishEventForValidOrders() {
        UUID validOrderId = UUID.randomUUID();
        UUID invalidOrderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CanceledOrder validCanceledOrder = CanceledOrder.createCanceledOrder(
                validOrderId,
                memberId,
                sellerId,
                20_000L,
                0L,
                0L,
                20_000L,
                "policy-valid",
                "snapshot",
                CancelStatus.REQUESTED,
                CancelReason.CHANGE_OF_MIND,
                "reason",
                "idem-valid",
                PaymentMethod.PG
        );

        CanceledOrder skippedCanceledOrder = CanceledOrder.createCanceledOrder(
                invalidOrderId,
                memberId,
                sellerId,
                25_000L,
                0L,
                0L,
                25_000L,
                "policy-skip",
                "snapshot",
                CancelStatus.APPROVED,
                CancelReason.CHANGE_OF_MIND,
                "reason",
                "idem-skip",
                PaymentMethod.PG
        );

        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of(validCanceledOrder, skippedCanceledOrder));

        Order validOrder = mock(Order.class);
        when(orderRepository.findById(validOrderId)).thenReturn(Optional.of(validOrder));
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());
        when(validOrder.getGroupPurchaseId()).thenReturn(groupPurchaseId);

        OrderCancellationPolicy policy = mock(OrderCancellationPolicy.class);
        when(orderCancellationPolicyResolver.resolveByPolicyId("policy-valid")).thenReturn(policy);

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);
        when(groupPurchaseService.findByGroupPurchase(groupPurchaseId)).thenReturn(groupPurchase);

        when(productService.findByProductName(productId)).thenReturn("product-name");

        ArgumentCaptor<OrderCancelProcessedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelProcessedEvent.class);

        // when
        canceledOrderService.retryCancelOrder();

        // then
        verify(orderRepository).findById(validOrderId);
        verify(orderRepository).findById(invalidOrderId);
        verify(orderCancellationPolicyResolver).resolveByPolicyId("policy-valid");
        verify(groupPurchaseService).findByGroupPurchase(groupPurchaseId);
        verify(productService).findByProductName(productId);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderCancelProcessedEvent event = eventCaptor.getValue();
        assertThat(event.canceledOrder()).isEqualTo(validCanceledOrder);
        assertThat(event.productName()).isEqualTo("product-name");
    }

    @Test
    @DisplayName("자동 취소 조회는 PENDING 상태와 2일 이전을 기준으로 수행한다")
    void autoCancelOrder_filtersStatusAndTime() {
        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of());

        ArgumentCaptor<List<CancelStatus>> statusCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OffsetDateTime> timeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        canceledOrderService.autoCancelOrder();

        verify(canceledOrderRepository)
                .findAllByStatusInAndCanceledAtBefore(statusCaptor.capture(), timeCaptor.capture());

        assertThat(statusCaptor.getValue()).containsExactly(CancelStatus.PENDING);
        long days = Duration.between(timeCaptor.getValue(), OffsetDateTime.now()).toDays();
        assertThat(days).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("자동 취소 대상이 없으면 작업을 종료한다")
    void autoCancelOrder_noPendingOrders() {
        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of());

        canceledOrderService.autoCancelOrder();

        verify(orderRepository, never()).findById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("자동 취소 재시도 시 주문을 찾을 수 없으면 건너뛴다")
    void autoCancelOrder_skipWhenOrderMissing() {
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                sellerId,
                12_000L,
                0L,
                0L,
                12_000L,
                "policy",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "reason",
                "idem-key",
                PaymentMethod.PG
        );

        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        canceledOrderService.autoCancelOrder();

        verify(orderRepository).findById(orderId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("자동 취소 재시도 시 정책을 찾을 수 없으면 건너뛴다")
    void autoCancelOrder_skipWhenPolicyMissing() {
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                orderId,
                memberId,
                sellerId,
                18_000L,
                0L,
                0L,
                18_000L,
                "policy",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "reason",
                "idem-key",
                PaymentMethod.PG
        );

        Order order = mock(Order.class);

        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of(canceledOrder));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderCancellationPolicyResolver.resolveByPolicyId("policy")).thenReturn(null);

        canceledOrderService.autoCancelOrder();

        verify(orderRepository).findById(orderId);
        verify(orderCancellationPolicyResolver).resolveByPolicyId("policy");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("자동 취소 대상은 승인 처리 후 이벤트를 발행한다")
    void autoCancelOrder_publishEventForValidOrders() {
        UUID validOrderId = UUID.randomUUID();
        UUID invalidOrderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CanceledOrder validCanceledOrder = CanceledOrder.createCanceledOrder(
                validOrderId,
                memberId,
                sellerId,
                22_000L,
                1_000L,
                0L,
                21_000L,
                "policy-valid",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.CHANGE_OF_MIND,
                "reason",
                "idem-valid",
                PaymentMethod.POINT
        );

        CanceledOrder skippedCanceledOrder = CanceledOrder.createCanceledOrder(
                invalidOrderId,
                memberId,
                sellerId,
                30_000L,
                1_000L,
                0L,
                29_000L,
                "policy-valid",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "reason",
                "idem-skip",
                PaymentMethod.PG
        );

        when(canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(anyList(), any()))
                .thenReturn(List.of(validCanceledOrder, skippedCanceledOrder));

        Order validOrder = mock(Order.class);
        when(orderRepository.findById(validOrderId)).thenReturn(Optional.of(validOrder));
        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());
        when(validOrder.getGroupPurchaseId()).thenReturn(groupPurchaseId);

        OrderCancellationPolicy policy = mock(OrderCancellationPolicy.class);
        when(orderCancellationPolicyResolver.resolveByPolicyId("policy-valid")).thenReturn(policy);

        GroupPurchase groupPurchase = mock(GroupPurchase.class);
        when(groupPurchase.getProductId()).thenReturn(productId);
        when(groupPurchaseService.findByGroupPurchase(groupPurchaseId)).thenReturn(groupPurchase);

        when(productService.findByProductName(productId)).thenReturn("product-name");

        ArgumentCaptor<OrderCancelProcessedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelProcessedEvent.class);

        canceledOrderService.autoCancelOrder();

        verify(orderRepository).findById(validOrderId);
        verify(orderRepository).findById(invalidOrderId);
        verify(groupPurchaseService).findByGroupPurchase(groupPurchaseId);
        verify(productService).findByProductName(productId);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        OrderCancelProcessedEvent event = eventCaptor.getValue();
        assertThat(event.canceledOrder()).isEqualTo(validCanceledOrder);
        assertThat(event.productName()).isEqualTo("product-name");
        assertThat(validCanceledOrder.getStatus()).isEqualTo(CancelStatus.APPROVED);
    }
}
