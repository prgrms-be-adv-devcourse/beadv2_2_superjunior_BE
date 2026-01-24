package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.point.application.dto.pg.PgCreateCommand;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.infrastructure.pg.PgPaymentCancelJpaRepository;
import store._0982.point.infrastructure.pg.PgPaymentJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PgPaymentServiceConcurrencyTest extends BaseConcurrencyTest {

    @Autowired
    private PgPaymentService pgPaymentService;

    @Autowired
    private PgPaymentJpaRepository pgPaymentRepository;

    @Autowired
    private PgPaymentCancelJpaRepository pgPaymentCancelRepository;

    @BeforeEach
    void setUp() {
        pgPaymentCancelRepository.deleteAll();
        pgPaymentRepository.deleteAll();
    }

    @Test
    @DisplayName("결제 생성이 여러 번 요청되었을 때 하나의 객체만 생성한다")
    void concurrent_create() throws InterruptedException {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgCreateCommand command = new PgCreateCommand(orderId, 1000L);

        // when
        runSynchronizedTask(() -> pgPaymentService.createPayment(command, memberId));

        // then
        List<PgPayment> pgPayments = pgPaymentRepository.findAll();
        assertThat(pgPayments).singleElement()
                .extracting(PgPayment::getOrderId, PgPayment::getMemberId, PgPayment::getAmount)
                .containsExactly(orderId, memberId, 1000L);
    }
}
