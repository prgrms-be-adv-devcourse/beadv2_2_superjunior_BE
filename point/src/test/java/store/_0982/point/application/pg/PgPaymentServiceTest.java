package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.point.application.dto.pg.PgCreateCommand;
import store._0982.point.application.dto.pg.PgCreateInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgPaymentServiceTest {

    private static final String SAMPLE_PURCHASE_NAME = "테스트 공구";

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @InjectMocks
    private PgPaymentService pgPaymentService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("포인트 충전 주문을 생성한다")
    void createPaymentPoint_success() {
        // given
        PgCreateCommand command = new PgCreateCommand(orderId, 10000, SAMPLE_PURCHASE_NAME);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(pgPaymentRepository.saveAndFlush(any(PgPayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PgCreateInfo result = pgPaymentService.createPayment(command, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.amount()).isEqualTo(10000);
        verify(pgPaymentRepository).saveAndFlush(any(PgPayment.class));
    }

    @Test
    @DisplayName("이미 존재하는 주문번호로 생성 요청 시 기존 정보를 반환한다")
    void createPaymentPoint_returnExisting() {
        // given
        PgCreateCommand command = new PgCreateCommand(orderId, 10000, SAMPLE_PURCHASE_NAME);

        PgPayment existingPgPayment = PgPayment.create(memberId, orderId, 10000, SAMPLE_PURCHASE_NAME);
        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPgPayment));

        // when
        PgCreateInfo result = pgPaymentService.createPayment(command, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        verify(pgPaymentRepository, never()).saveAndFlush(any());
    }
}
