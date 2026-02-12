package store._0982.commerce.application.settlement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.commerce.domain.settlement.OrderSettlementRepository;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CanceledOrder;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.settlement.OrderSettlement;
import store._0982.common.domain.settlement.OrderSettlementStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSettlementServiceTest {

    @Mock
    private OrderSettlementRepository orderSettlementRepository;

    @InjectMocks
    private OrderSettlementService orderSettlementService;

    @Test
    @DisplayName("주문 확정 시 정산 정보를 저장한다")
    void saveConfirmedOrderSettlement_success() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getSellerId()).thenReturn(sellerId);
        when(order.getGroupPurchaseId()).thenReturn(groupPurchaseId);
        when(order.getPaidPrice()).thenReturn(20_000L);

        ArgumentCaptor<OrderSettlement> captor = ArgumentCaptor.forClass(OrderSettlement.class);

        // when
        orderSettlementService.saveConfirmedOrderSettlement(order);

        // then
        verify(orderSettlementRepository).save(captor.capture());
        OrderSettlement saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getSellerId()).isEqualTo(sellerId);
        assertThat(saved.getGroupPurchaseId()).isEqualTo(groupPurchaseId);
        assertThat(saved.getOrderAmount()).isEqualTo(20_000L);
        assertThat(saved.getStatus()).isEqualTo(OrderSettlementStatus.COMPLETED);
        assertThat(saved.getSettlementAmount()).isEqualTo(20_000L - (long) (20_000L * 0.2));
    }

    @Test
    @DisplayName("구매자 귀책 취소 시 취소 수수료 정산을 저장한다")
    void saveCanceledOrderSettlement_buyerFault() {
        // given
        UUID canceledOrderId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getSellerId()).thenReturn(sellerId);
        when(order.getGroupPurchaseId()).thenReturn(groupPurchaseId);

        CanceledOrder canceledOrder = mock(CanceledOrder.class);
        when(canceledOrder.getReason()).thenReturn(CancelReason.CHANGE_OF_MIND);
        when(canceledOrder.getCancelFeeAmount()).thenReturn(15_000L);
        when(canceledOrder.getOrderId()).thenReturn(canceledOrderId);

        ArgumentCaptor<OrderSettlement> captor = ArgumentCaptor.forClass(OrderSettlement.class);

        // when
        orderSettlementService.saveCanceledOrderSettlement(order, canceledOrder);

        // then
        verify(orderSettlementRepository).save(captor.capture());
        OrderSettlement saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(canceledOrderId);
        assertThat(saved.getSellerId()).isEqualTo(sellerId);
        assertThat(saved.getSettlementAmount()).isEqualTo(15_000L);
        assertThat(saved.getStatus()).isEqualTo(OrderSettlementStatus.BUYER_CANCEL);
    }

    @Test
    @DisplayName("판매자 귀책인 경우 정산을 저장하지 않는다")
    void saveCanceledOrderSettlement_sellerFault() {
        // given
        Order order = mock(Order.class);
        CanceledOrder canceledOrder = mock(CanceledOrder.class);
        when(canceledOrder.getReason()).thenReturn(CancelReason.OUT_OF_STOCK);

        // when
        orderSettlementService.saveCanceledOrderSettlement(order, canceledOrder);

        // then
        verify(orderSettlementRepository, never()).save(any());
    }

    @Test
    @DisplayName("쇼핑물 귀책인 경우 정산을 저장하지 않는다")
    void saveCanceledOrderSettlement_noActionWhenOtherReason() {
        // given
        Order order = mock(Order.class);
        CanceledOrder canceledOrder = mock(CanceledOrder.class);
        when(canceledOrder.getReason()).thenReturn(CancelReason.GROUP_PURCHASE_FAILED);

        // when
        orderSettlementService.saveCanceledOrderSettlement(order, canceledOrder);

        // then
        verify(orderSettlementRepository, never()).save(any());
    }
}
