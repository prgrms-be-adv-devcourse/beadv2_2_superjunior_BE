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
import store._0982.point.application.dto.PointChargeCommand;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointBalanceInfo;
import store._0982.point.client.CommerceServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointChargedTxEvent;
import store._0982.point.domain.event.PointDeductedTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class PointReadServiceTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CommerceServiceClient commerceServiceClient;

    @InjectMocks
    private PointReadService pointReadService;

    private UUID memberId;
    private UUID orderId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Nested
    @DisplayName("포인트 조회")
    class GetPoints {

        @Test
        @DisplayName("포인트를 조회한다")
        void getPoints_success() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

            // when
            PointBalanceInfo result = pointReadService.getPoints(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.paidPoint()).isEqualTo(10000);
        }

        @Test
        @DisplayName("포인트가 없는 회원 조회 시 예외가 발생한다")
        void getPoints_fail_whenMemberPointNotFound() {
            // given
            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointReadService.getPoints(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePoints {

        @Test
        @DisplayName("포인트를 충전한다")
        void chargePoints_success() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);
            PointTransaction history = PointTransaction.charged(memberId, idempotencyKey, PointAmount.of(10000, 0));

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(history);
            doNothing().when(applicationEventPublisher).publishEvent(any(PointChargedTxEvent.class));

            // when
            PointBalanceInfo result = pointReadService.chargePoints(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000);
            verify(applicationEventPublisher).publishEvent(any(PointChargedTxEvent.class));
        }

        @Test
        @DisplayName("중복 충전 요청은 멱등성 키로 중복 처리하지 않는다")
        void chargePoints_idempotent() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(5000);

            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

            // when
            PointBalanceInfo result = pointReadService.chargePoints(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(5000); // 충전되지 않음
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 충전 시 예외가 발생한다")
        void chargePoints_fail_whenMemberNotFound() {
            // given
            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointReadService.chargePoints(command, memberId))
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
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            PointAmount deduction = PointAmount.of(5000, 0);
            PointTransaction history = PointTransaction.used(memberId, orderId, idempotencyKey, deduction);
            OrderInfo orderInfo = new OrderInfo(orderId, 5000, OrderInfo.Status.PENDING, memberId, 1);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(false);
            when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(history);
            doNothing().when(applicationEventPublisher).publishEvent(any(PointDeductedTxEvent.class));

            // when
            PointBalanceInfo result = pointReadService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(5000);
            verify(applicationEventPublisher).publishEvent(any(PointDeductedTxEvent.class));
        }

        @Test
        @DisplayName("네트워크 장애 등에 의한 중복 차감 요청은 멱등성 키로 중복 처리하지 않는다")
        void use_idempotent() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

            // when
            PointBalanceInfo result = pointReadService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000); // 차감되지 않음
            verify(commerceServiceClient, never()).getOrder(any(), any());
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("사용자 실수 등에 의한 중복 차감 요청은 주문 검사로 중복 처리하지 않는다")
        void use_duplicate() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(true);

            // when
            PointBalanceInfo result = pointReadService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000); // 차감되지 않음
            verify(commerceServiceClient, never()).getOrder(any(), any());
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 차감 시 예외가 발생한다")
        void use_fail_whenMemberNotFound() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointReadService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("잔액 부족 시 차감이 실패한다")
        void use_fail_whenInsufficientBalance() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(3000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            OrderInfo orderInfo = new OrderInfo(orderId, 5000, OrderInfo.Status.PENDING, memberId, 1);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(false);
            when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

            // when & then
            assertThatThrownBy(() -> pointReadService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("주문 금액과 차감 금액이 다를 때 예외가 발생한다")
        void use_fail_whenAmountMismatch() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            OrderInfo orderInfo = new OrderInfo(orderId, 3000, OrderInfo.Status.PENDING, memberId, 1);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(false);
            when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

            // when & then
            assertThatThrownBy(() -> pointReadService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_REQUEST.getMessage());
        }

        @Test
        @DisplayName("다른 회원의 주문으로 차감 시 예외가 발생한다")
        void use_fail_whenOrderOwnerMismatch() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            UUID otherMemberId = UUID.randomUUID();
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            OrderInfo orderInfo = new OrderInfo(orderId, 5000, OrderInfo.Status.PENDING, otherMemberId, 1);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(false);
            when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

            // when & then
            assertThatThrownBy(() -> pointReadService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_REQUEST.getMessage());
        }

        @Test
        @DisplayName("취소된 주문으로 차감 시 예외가 발생한다")
        void use_fail_whenOrderCanceled() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            OrderInfo orderInfo = new OrderInfo(orderId, 5000, OrderInfo.Status.CANCELLED, memberId, 1);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(false);
            when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

            // when & then
            assertThatThrownBy(() -> pointReadService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_REQUEST.getMessage());
        }

        @Test
        @DisplayName("포인트 차감 시 올바른 이벤트가 발행된다")
        void use_publishCorrectEvent() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            PointAmount deduction = PointAmount.of(5000, 0);
            PointTransaction history = PointTransaction.used(memberId, orderId, idempotencyKey, deduction);
            OrderInfo orderInfo = new OrderInfo(orderId, 5000, OrderInfo.Status.PENDING, memberId, 1);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)).thenReturn(false);
            when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(history);

            ArgumentCaptor<PointDeductedTxEvent> eventCaptor = ArgumentCaptor.forClass(PointDeductedTxEvent.class);

            // when
            pointReadService.deductPoints(memberId, command);

            // then
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            PointDeductedTxEvent capturedEvent = eventCaptor.getValue();

            assertThat(capturedEvent).isNotNull();
            assertThat(capturedEvent.history()).isNotNull();
            assertThat(capturedEvent.history().getMemberId()).isEqualTo(memberId);
            assertThat(capturedEvent.history().getOrderId()).isEqualTo(orderId);
            assertThat(capturedEvent.history().getStatus()).isEqualTo(PointTransactionStatus.USED);
            assertThat(capturedEvent.history().getPointAmount().getTotal()).isEqualTo(5000);
        }
    }
}
