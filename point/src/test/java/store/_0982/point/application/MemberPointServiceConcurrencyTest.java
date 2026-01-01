package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.repository.MemberPointRepository;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka
class MemberPointServiceConcurrencyTest {

    @Autowired
    private MemberPointService memberPointService;

    @Autowired
    private MemberPointRepository memberPointRepository;

    @Autowired
    private MemberPointHistoryRepository memberPointHistoryRepository;

    @MockitoBean
    private OrderServiceClient orderServiceClient;

    @BeforeEach
    void setUp() {
        memberPointRepository.deleteAll();
        memberPointHistoryRepository.deleteAll();
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 차감 요청을 중복 처리하지 않는다")
    void concurrent_deduct() throws InterruptedException {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(100000);
        memberPointRepository.save(memberPoint);

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        PointDeductCommand command = new PointDeductCommand(UUID.randomUUID(), UUID.randomUUID(), 1000);
        OrderInfo orderInfo = new OrderInfo(command.orderId(), command.amount(), OrderInfo.Status.IN_PROGRESS, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    memberPointService.deductPoints(memberId, command);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(memberPointHistoryRepository.count()).isEqualTo(1);
    }
}
