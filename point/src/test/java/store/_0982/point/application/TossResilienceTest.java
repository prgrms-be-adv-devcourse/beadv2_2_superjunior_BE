package store._0982.point.application;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.application.pg.PgTxManager;
import store._0982.point.client.TossPaymentClient;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.support.BaseIntegrationTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class TossResilienceTest extends BaseIntegrationTest {

    private static final String TEST_PAYMENT_KEY = "test-key";
    private static final long DEFAULT_AMOUNT = 1000;

    @Autowired
    private TossPaymentClient tossPaymentClient;

    @Autowired
    private TossPaymentService tossPaymentService;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private PgTxManager pgTxManager;

    @Test
    @DisplayName("Retry 검증: 타임아웃 발생 시 지정된 횟수(3회)만큼 재시도한다")
    void retryTest() {
        // given
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new ResourceAccessException("Read timed out"));

        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(UUID.randomUUID(), DEFAULT_AMOUNT, TEST_PAYMENT_KEY);

        // when & then
        assertThatThrownBy(() -> tossPaymentClient.confirm(request))
                .isInstanceOf(ResourceAccessException.class);
        verify(restTemplate, times(3)).postForObject(anyString(), any(), any());
    }

    @Test
    @DisplayName("RateLimiter 검증: 초당 제한(30 TPS)을 넘으면 요청이 거절된다")
    void rateLimiterTest() throws InterruptedException {
        // given
        int threadCount = 35;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgConfirmCommand command = new PgConfirmCommand(orderId, DEFAULT_AMOUNT, TEST_PAYMENT_KEY);
        PgPayment pgPayment = PgPayment.create(memberId, orderId, DEFAULT_AMOUNT);

        when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(null);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    tossPaymentService.confirmPayment(pgPayment, command);
                    successCount.incrementAndGet();
                } catch (RequestNotPermitted e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        System.out.println("Success: " + successCount.get() + ", Blocked: " + failCount.get());
        assertThat(successCount.get()).isEqualTo(30);
    }
}
