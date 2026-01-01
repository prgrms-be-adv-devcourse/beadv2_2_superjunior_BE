package store._0982.point.application;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.repository.MemberPointRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MemberPointServiceConcurrencyTest {
    private static final int BALANCE = 100_000;
    private static final int AMOUNT = 1_000;
    private static final int THREAD_COUNT = 10;

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private MemberPointService memberPointService;

    @Autowired
    private MemberPointRepository memberPointRepository;

    @Autowired
    private MemberPointHistoryRepository memberPointHistoryRepository;

    @MockitoBean
    private OrderServiceClient orderServiceClient;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberPointRepository.deleteAll();
        memberPointHistoryRepository.deleteAll();

        memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(BALANCE);
        memberPointRepository.save(memberPoint);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 차감 요청을 중복 처리하지 않는다")
    void concurrent_deduct_idempotent() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        PointDeductCommand command = new PointDeductCommand(UUID.randomUUID(), UUID.randomUUID(), AMOUNT);
        OrderInfo orderInfo = new OrderInfo(command.orderId(), command.amount(), OrderInfo.Status.IN_PROGRESS, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    memberPointService.deductPoints(memberId, command);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        validate(true);
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 차감 요청을 중복 처리하지 않는다")
    void concurrent_deduct_duplicate() throws InterruptedException {
        // given
        UUID orderId = UUID.randomUUID();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        List<PointDeductCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PointDeductCommand.class)
                .instantiate(Instantiator.constructor())
                .set("amount", AMOUNT)
                .set("orderId", orderId)
                .setNotNull("idempotencyKey")
                .sampleList(THREAD_COUNT);

        OrderInfo orderInfo = new OrderInfo(orderId, AMOUNT, OrderInfo.Status.FAILED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        for (PointDeductCommand command : commands) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    memberPointService.deductPoints(memberId, command);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        validate(true);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 반환 요청을 중복 처리하지 않는다")
    void concurrent_return_idempotent() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        PointReturnCommand command = new PointReturnCommand(UUID.randomUUID(), UUID.randomUUID(), AMOUNT);
        OrderInfo orderInfo = new OrderInfo(command.orderId(), command.amount(), OrderInfo.Status.FAILED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    memberPointService.returnPoints(memberId, command);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        validate(false);
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 반환 요청을 중복 처리하지 않는다")
    void concurrent_return_duplicate() throws InterruptedException {
        // given
        UUID orderId = UUID.randomUUID();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        List<PointReturnCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PointReturnCommand.class)
                .instantiate(Instantiator.constructor())
                .set("amount", AMOUNT)
                .set("orderId", orderId)
                .setNotNull("idempotencyKey")
                .sampleList(THREAD_COUNT);

        OrderInfo orderInfo = new OrderInfo(orderId, AMOUNT, OrderInfo.Status.FAILED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        for (PointReturnCommand command : commands) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    memberPointService.returnPoints(memberId, command);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        validate(false);
    }

    private void validate(boolean isDeduct) {
        assertThat(memberPointHistoryRepository.count()).isEqualTo(1);
        MemberPoint memberPoint = memberPointRepository.findById(memberId).orElseThrow();
        assertThat(memberPoint.getPointBalance()).isEqualTo(isDeduct ? BALANCE - AMOUNT : BALANCE + AMOUNT);
    }
}
