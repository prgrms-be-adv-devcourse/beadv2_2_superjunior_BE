package store._0982.commerce.integration.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.UnexpectedRollbackException;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.integration.order.testsupport.RetryCancelService;
import store._0982.commerce.integration.order.testsupport.RetryQuantityService;
import store._0982.commerce.support.BaseIntegrationTest;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.PaymentMethod;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableRetry
class PartialRetryRollbackIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RetryCancelService retryCancelService;

    @Autowired
    private RetryQuantityService retryQuantityRetryService;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 재시도 카운터 초기화
        retryQuantityRetryService.resetAttemptCount();
    }

    @Test
    @DisplayName("@Retryable이 적용되어도 rollback-only 오염으로 재시도 후 UnexpectedRollbackException이 발생한다")
    void rollbackOnlyPollutionProof() {
        // Given
        Order order = persistOrder();

        // When & Then
        assertThatThrownBy(() -> retryCancelService.cancelLike(order.getOrderId(), order.getMemberId()))
                .isInstanceOf(UnexpectedRollbackException.class)
                .hasMessageContaining("Transaction silently rolled back because it has been marked as rollback-only");

        // 재시도가 실제로 일어났는지 검증
        // @Retryable(maxAttempts = 4) 설정으로 총 4번 시도해야 함
        assertThat(retryQuantityRetryService.getAttemptCount().get())
                .as("@Retryable이 설정대로 4번 재시도했는지 확인")
                .isEqualTo(4);

        System.out.println("검증 완료: @Retryable로 4번 재시도했지만, rollback-only 오염으로 최종 실패");
    }

    private Order persistOrder() {
        UUID memberId = UUID.randomUUID();
        Order order = Order.create(
                2,
                25_000L,
                25_000L,
                memberId,
                "Seoul address",
                "detail",
                "12345",
                "tester",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID().toString()
        );
        order.completePayment(PaymentMethod.PG);
        return orderRepository.save(order);
    }
}
