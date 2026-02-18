package store._0982.commerce.integration.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import store._0982.commerce.support.concurrency.BenchmarkStats;
import store._0982.commerce.support.concurrency.ConcurrencyResult;
import store._0982.common.domain.order.Order;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("benchmark") // 기본 test 단계에선 제외하고, 필요할 때만 실행
class OrderCancelConcurrencyBenchmarkTest extends AbstractOrderCancelConcurrencySupport {

    @Test
    @DisplayName("벤치: 100개 주문 동시 취소 (Load 32 threads) - warmup/repeat")
    void benchmark_cancel_100_orders_load_32() throws Exception {
        int orderCount = 100;

        // warmup 3회, measure 10회
        int warmup = 3;
        int repeats = 10;

        for (int i = 0; i < warmup; i++) {
            runOneScenario(orderCount, 2, 32, "warmup-" + (i + 1));
        }

        List<ConcurrencyResult> measurements = new ArrayList<>();
        for (int i = 0; i < repeats; i++) {
            ConcurrencyResult r = runOneScenario(orderCount, 2, 32, "run-" + (i + 1));
            measurements.add(r);
            assertThat(r.failCount()).isZero();
        }

        // 통계 계산
        BenchmarkStats stats = BenchmarkStats.from("100개 주문 취소 (32 threads) - modifying", measurements);
        stats.printReport();
    }

    private ConcurrencyResult runOneScenario(int orderCount, int qtyPerOrder, int threads, String name) throws Exception {
        List<Order> orders = createMultipleOrders(orderCount, qtyPerOrder);
        increaseGroupPurchaseQuantityByOrders(orders);

        var commands = buildCancelCommandsForOrders(orders);
        ConcurrencyResult result = runLoad(threads, commands);

        // 정합성도 최소한 확인
        entityManager.clear();
        assertThat(groupPurchaseRepository.findById(groupPurchaseId).orElseThrow().getCurrentQuantity()).isEqualTo(0);

        return result;
    }
}
