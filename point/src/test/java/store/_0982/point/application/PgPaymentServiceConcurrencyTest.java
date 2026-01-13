package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.PgCreateCommand;
import store._0982.point.application.pg.PgPaymentService;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.infrastructure.PgPaymentJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PgPaymentServiceConcurrencyTest extends BaseConcurrencyTest {

    @Autowired
    private PgPaymentService pgPaymentService;

    @Autowired
    private PgPaymentJpaRepository paymentPointRepository;

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
        PgCreateCommand command = new PgCreateCommand(orderId, 1000L);

        // when
        runSynchronizedTask(() -> pgPaymentService.createPayment(command, memberId));

        // then
        List<PgPayment> pgPayments = paymentPointRepository.findAll();
        assertThat(pgPayments).singleElement()
                .extracting(PgPayment::getPgOrderId, PgPayment::getMemberId, PgPayment::getAmount)
                .containsExactly(orderId, memberId, 1000L);
    }
}
