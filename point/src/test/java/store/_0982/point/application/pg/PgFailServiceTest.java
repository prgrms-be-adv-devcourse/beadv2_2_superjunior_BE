package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgFailServiceTest {

    @Mock
    private PgTxManager pgTxManager;

    @InjectMocks
    private PgFailService pgFailService;

    private UUID memberId;
    private PgFailCommand command;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        command = new PgFailCommand(
                UUID.randomUUID(),
                "test_payment_key",
                "PAYMENT_FAILED",
                "카드 승인 실패",
                10000L,
                "{}"
        );
    }

    @Test
    @DisplayName("결제 실패 정보를 저장한다")
    void handlePaymentFailure_success() {
        // given
        doNothing().when(pgTxManager).markFailedPaymentByPg(command, memberId);

        // when
        pgFailService.handlePaymentFailure(command, memberId);

        // then
        verify(pgTxManager).markFailedPaymentByPg(command, memberId);
    }

    @Test
    @DisplayName("이미 성공(COMPLETED)된 결제를 실패 처리하려 하면 예외가 발생한다")
    void handlePaymentFailure_exception_when_completed() {
        // given
        doThrow(new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE))
                .when(pgTxManager).markFailedPaymentByPg(command, memberId);

        // when & then
        assertThatThrownBy(() -> pgFailService.handlePaymentFailure(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.CANNOT_HANDLE_FAILURE.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 실패 처리 요청 시 예외가 발생한다")
    void handlePaymentFailure_not_found() {
        // given
        doThrow(new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND))
                .when(pgTxManager).markFailedPaymentByPg(command, memberId);

        // when & then
        assertThatThrownBy(() -> pgFailService.handlePaymentFailure(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }
}
