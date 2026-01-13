package store._0982.point.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PointInfo;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.application.point.PointTransactionService;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointTransactionServiceTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private PointTransactionService pointTransactionService;

    @Nested
    @DisplayName("포인트 조회")
    class GetPoints {

        @Test
        @DisplayName("포인트를 조회한다")
        void getPoints_success() {
            // given
            UUID memberId = UUID.randomUUID();
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));

            // when
            PointInfo result = pointTransactionService.getPoints(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.paidPoint()).isEqualTo(10000);
        }

        @Test
        @DisplayName("포인트가 없는 회원 조회 시 예외가 발생한다")
        void getPoints_fail_whenMemberPointNotFound() {
            // given
            UUID memberId = UUID.randomUUID();

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointTransactionService.getPoints(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 차감")
    class DeductPoints {

        @Test
        @DisplayName("포인트를 차감한다")
        void use_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            PointTransaction history = PointTransaction.used(memberId, command);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(history);
            doNothing().when(applicationEventPublisher).publishEvent(any(PointDeductedEvent.class));

            // when
            PointInfo result = pointTransactionService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(5000);
            verify(applicationEventPublisher).publishEvent(any(PointDeductedEvent.class));
        }

        @Test
        @DisplayName("네트워크 장애 등에 의한 중복 차감 요청은 멱등성 키로 중복 처리하지 않는다")
        void use_idempotent() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

            // when
            PointInfo result = pointTransactionService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000); // 차감되지 않음
            verify(orderServiceClient, never()).getOrder(any(), any());
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("사용자 실수 등에 의한 중복 차감 요청은 주문 검사로 중복 처리하지 않는다")
        void use_duplicate() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(true);

            // when
            PointInfo result = pointTransactionService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000); // 차감되지 않음
            verify(orderServiceClient, never()).getOrder(any(), any());
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 차감 시 예외가 발생한다")
        void use_fail_whenMemberNotFound() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, UUID.randomUUID(), 5000);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointTransactionService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 반환")
    class ReturnPoints {

        @Test
        @DisplayName("포인트를 반환한다")
        void returnPoints_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(5000);

            PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, 3000);
            PointTransaction history = PointTransaction.returned(memberId, command);
            OrderInfo orderInfo = new OrderInfo(orderId, 3000, OrderInfo.Status.ORDER_FAILED, memberId, 1);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(history);
            when(orderServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

            // when
            PointInfo result = pointTransactionService.returnPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(8000);
            verify(applicationEventPublisher).publishEvent(any(PointReturnedEvent.class));
        }

        @Test
        @DisplayName("이미 처리된 반환 요청은 멱등성 키로 중복 처리하지 않는다")
        void returnPoints_idempotent() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(5000);

            PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, 3000);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

            // when
            PointInfo result = pointTransactionService.returnPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(5000); // 반환되지 않음
            verify(orderServiceClient, never()).getOrder(any(), any());
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 반환 시 예외가 발생한다")
        void returnPoints_fail_whenMemberNotFound() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            PointReturnCommand command = new PointReturnCommand(idempotencyKey, UUID.randomUUID(), 3000);

            when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointTransactionService.returnPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
