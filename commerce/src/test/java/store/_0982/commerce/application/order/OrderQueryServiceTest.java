package store._0982.commerce.application.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.infrastructure.product.ProductVectorJpaRepository;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.CanceledOrder;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.common.dto.PageResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CanceledOrderRepository canceledOrderRepository;

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVectorJpaRepository productVectorRepository;

    @Mock
    private GroupPurchaseService groupPurchaseService;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Test
    @DisplayName("회원의 취소 내역을 페이지 형태로 조회한다")
    void getCanceledOrders_success() {
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 2);
        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                UUID.randomUUID(),
                memberId,
                UUID.randomUUID(),
                50_000L,
                5_000L,
                3_000L,
                42_000L,
                "policy",
                "snapshot",
                CancelStatus.REQUESTED,
                CancelReason.CHANGE_OF_MIND,
                "detail",
                "idem-key",
                PaymentMethod.PG
        );
        Page<CanceledOrder> page = new PageImpl<>(List.of(canceledOrder), pageable, 1);
        when(canceledOrderRepository.findAllByMemberId(memberId, pageable)).thenReturn(page);

        PageResponse<OrderCancelInfo> response = orderQueryService.getCanceledOrders(memberId, pageable);

        assertThat(response.content()).hasSize(1);
        OrderCancelInfo info = response.content().get(0);
        assertThat(info.orderId()).isEqualTo(canceledOrder.getOrderId());
        assertThat(info.status()).isEqualTo(CancelStatus.REQUESTED);
        assertThat(info.refundAmount()).isEqualTo(42_000L);
        assertThat(response.numberOfElements()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(pageable.getPageSize());
    }

    @Test
    @DisplayName("판매자 대기중 취소 내역을 페이지 형태로 조회한다")
    void getPendingOrder_success() {
        UUID sellerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 5);
        CanceledOrder pendingOrder = CanceledOrder.createCanceledOrder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                sellerId,
                70_000L,
                0L,
                0L,
                70_000L,
                "policy",
                "snapshot",
                CancelStatus.PENDING,
                CancelReason.PRODUCT_DEFECT,
                "pending",
                "idem-key",
                PaymentMethod.POINT
        );

        Page<CanceledOrder> page = new PageImpl<>(List.of(pendingOrder), pageable, 1);
        when(canceledOrderRepository.findAllBySellerIdAndStatus(sellerId, CancelStatus.PENDING, pageable))
                .thenReturn(page);

        PageResponse<OrderCancelInfo> response = orderQueryService.getPendingOrder(sellerId, pageable);

        assertThat(response.content()).hasSize(1);
        OrderCancelInfo info = response.content().get(0);
        assertThat(info.status()).isEqualTo(CancelStatus.PENDING);
        assertThat(info.orderId()).isEqualTo(pendingOrder.getOrderId());
    }
}
