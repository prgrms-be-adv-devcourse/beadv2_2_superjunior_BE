package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.PaymentPointCommand;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.infrastructure.PaymentPointJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentPointServiceConcurrencyTest extends BaseConcurrencyTest {

    @Autowired
    private PaymentPointService paymentPointService;

    @Autowired
    private PaymentPointJpaRepository paymentPointRepository;

    @MockitoBean
    private TossPaymentService tossPaymentService;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        paymentPointRepository.deleteAll();
    }

    @Test
    @DisplayName("포인트 충전 요청이 여러 번 요청되었을 때 하나의 객체만 생성한다")
    void concurrent_create() throws InterruptedException {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPointCommand command = new PaymentPointCommand(orderId, 1000L);

        // when
        runSynchronizedTask(() -> paymentPointService.createPaymentPoint(command, memberId));

        // then
        List<PaymentPoint> paymentPoints = paymentPointRepository.findAll();
        assertThat(paymentPoints).singleElement()
                .extracting(PaymentPoint::getPgOrderId, PaymentPoint::getMemberId, PaymentPoint::getAmount)
                .containsExactly(orderId, memberId, 1000L);
    }
}
