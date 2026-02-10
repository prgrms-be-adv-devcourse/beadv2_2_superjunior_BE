package store._0982.commerce.application.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.RefundOrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.ReversalOrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.VoidOrderCancellationPolicy;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.Order;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancellationPolicyResolverTest {

    private final VoidOrderCancellationPolicy voidPolicy = new VoidOrderCancellationPolicy();
    private final ReversalOrderCancellationPolicy reversalPolicy = new ReversalOrderCancellationPolicy();
    private final RefundOrderCancellationPolicy refundPolicy = new RefundOrderCancellationPolicy();

    private OrderCancellationPolicyResolver resolver;

    @Mock
    private GroupPurchase groupPurchase;
    @Mock
    private Order order;

    @BeforeEach
    void setUp() {
        resolver = new OrderCancellationPolicyResolver(voidPolicy, reversalPolicy, refundPolicy);
    }

    @Test
    @DisplayName("구매자 귀책 + void 기간에는 Void 정책을 반환한다")
    void resolve_returnsVoidPolicyDuringVoidPeriodForBuyerFault() {
        when(groupPurchase.isInVoidPeriod()).thenReturn(true);

        OrderCancellationPolicy policy = resolver.resolve(groupPurchase, order, CancelReason.CHANGE_OF_MIND);

        assertThat(policy).isSameAs(voidPolicy);
    }

    @Test
    @DisplayName("구매자 귀책 + reversed 기간에는 Reversal 정책을 반환한다")
    void resolve_returnsReversalPolicyForReversedPeriod() {
        OffsetDateTime canceledAt = OffsetDateTime.now();
        when(groupPurchase.isInVoidPeriod()).thenReturn(false);
        when(order.getCanceledAt()).thenReturn(canceledAt);
        when(groupPurchase.isInReversedPeriod(canceledAt)).thenReturn(true);

        OrderCancellationPolicy policy = resolver.resolve(groupPurchase, order, CancelReason.CHANGE_OF_MIND);

        assertThat(policy).isSameAs(reversalPolicy);
    }

    @Test
    @DisplayName("구매자 귀책 + returned 기간에는 Refund 정책을 반환한다")
    void resolve_returnsRefundPolicyForReturnedPeriod() {
        OffsetDateTime canceledAt = OffsetDateTime.now();
        when(groupPurchase.isInVoidPeriod()).thenReturn(false);
        when(order.getCanceledAt()).thenReturn(canceledAt);
        when(groupPurchase.isInReversedPeriod(canceledAt)).thenReturn(false);
        when(groupPurchase.isInReturnedPeriod(canceledAt)).thenReturn(true);

        OrderCancellationPolicy policy = resolver.resolve(groupPurchase, order, CancelReason.CHANGE_OF_MIND);

        assertThat(policy).isSameAs(refundPolicy);
    }

    @Test
    @DisplayName("판매자 귀책 사유는 항상 Void 정책을 반환한다")
    void resolve_returnsVoidPolicyForSellerFault() {
        OrderCancellationPolicy policy = resolver.resolve(groupPurchase, order, CancelReason.PRODUCT_DEFECT);

        assertThat(policy).isSameAs(voidPolicy);
    }

    @Test
    @DisplayName("정책 ID 로 정책을 조회할 수 있다")
    void resolveByPolicyId_returnsPolicy() {
        assertThat(resolver.resolveByPolicyId(voidPolicy.getPolicyId())).isSameAs(voidPolicy);
        assertThat(resolver.resolveByPolicyId(reversalPolicy.getPolicyId())).isSameAs(reversalPolicy);
        assertThat(resolver.resolveByPolicyId(refundPolicy.getPolicyId())).isSameAs(refundPolicy);
        assertThat(resolver.resolveByPolicyId("unknown")).isNull();
    }
}
