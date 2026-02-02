package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointChargeCommand;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointChargedTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointChargeServiceTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PointChargeService pointChargeService;

    private UUID memberId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePoints {

        @Test
        @DisplayName("포인트를 충전한다")
        void chargePoints_success() {
            // given
            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);
            PointBalance pointBalance = new PointBalance(memberId);
            
            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(applicationEventPublisher).publishEvent(any(PointChargedTxEvent.class));

            // when
            PointBalanceInfo result = pointChargeService.chargePoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000);
            assertThat(pointBalance.getPaidBalance()).isEqualTo(10000);
            verify(pointBalanceRepository).findByMemberId(memberId);
            verify(pointTransactionRepository).saveAndFlush(any(PointTransaction.class));
            verify(applicationEventPublisher).publishEvent(any(PointChargedTxEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 충전 시 예외가 발생한다")
        void chargePoints_fail_whenMemberNotFound() {
            // given
            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointChargeService.chargePoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
