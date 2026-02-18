package store._0982.commerce.integration.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.commerce.support.concurrency.ConcurrencyResult;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCancelConcurrencyCorrectnessTest extends AbstractOrderCancelConcurrencySupport {

    @Test
    @DisplayName("동일 공동구매: 서로 다른 주문 10개 동시 취소 - 모두 성공 & 수량 0")
    void cancel_10_orders_same_groupPurchase_all_success_and_quantity_zero() throws Exception {
        List<Order> orders = createMultipleOrders(10, 5);
        increaseGroupPurchaseQuantityByOrders(orders);

        GroupPurchase gpBefore = groupPurchaseRepository.findById(groupPurchaseId).orElseThrow();
        assertThat(gpBefore.getCurrentQuantity()).isEqualTo(50);

        var commands = buildCancelCommandsForOrders(orders);
        ConcurrencyResult result = runRace(commands);

        assertThat(result.successCount()).isEqualTo(commands.size());
        assertThat(result.failCount()).isZero();

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
    @DisplayName("동일 주문 10번 동시 취소 - CanceledOrder는 1개만 생성(멱등성)")
    void cancel_same_order_10_times_only_one_canceled_order() throws Exception {
        Order order = createSingleOrder(UUID.randomUUID(), 3);
        increaseGroupPurchaseQuantityByOrders(List.of(order));

        var commands = buildSameOrderCommands(order, 10);
        runRace(commands);

        entityManager.clear();
        assertThat(countCanceledOrderByOrderId(order.getOrderId())).isEqualTo(1L);
        Order refreshed = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
