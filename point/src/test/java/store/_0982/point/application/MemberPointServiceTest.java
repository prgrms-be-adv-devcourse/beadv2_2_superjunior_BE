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
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.MemberPointHistoryStatus;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberPointServiceTest {

    @Mock
    private MemberPointRepository memberPointRepository;

    @Mock
    private MemberPointHistoryRepository memberPointHistoryRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private MemberPointService memberPointService;

    @Nested
    @DisplayName("포인트 조회")
    class GetPoints {

        @Test
        @DisplayName("포인트를 조회한다")
        void getPoints_success() {
            // given
            UUID memberId = UUID.randomUUID();
            MemberPoint memberPoint = new MemberPoint(memberId);
            memberPoint.addPoints(10000);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));

            // when
            MemberPointInfo result = memberPointService.getPoints(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.pointBalance()).isEqualTo(10000);
        }

        @Test
        @DisplayName("포인트가 없는 회원 조회 시 예외가 발생한다")
        void getPoints_fail_whenMemberPointNotFound() {
            // given
            UUID memberId = UUID.randomUUID();

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberPointService.getPoints(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 차감")
    class DeductPoints {

        @Test
        @DisplayName("포인트를 차감한다")
        void deductPoints_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            MemberPoint memberPoint = new MemberPoint(memberId);
            memberPoint.addPoints(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            MemberPointHistory history = MemberPointHistory.used(memberId, command);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
            when(memberPointHistoryRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(memberPointHistoryRepository.save(any(MemberPointHistory.class))).thenReturn(history);
            doNothing().when(applicationEventPublisher).publishEvent(any(PointDeductedEvent.class));

            // when
            MemberPointInfo result = memberPointService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.pointBalance()).isEqualTo(5000);
            verify(applicationEventPublisher).publishEvent(any(PointDeductedEvent.class));
        }

        @Test
        @DisplayName("네트워크 장애 등에 의한 중복 차감 요청은 멱등성 키로 중복 처리하지 않는다")
        void deductPoints_idempotent() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            MemberPoint memberPoint = new MemberPoint(memberId);
            memberPoint.addPoints(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
            when(memberPointHistoryRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

            // when
            MemberPointInfo result = memberPointService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.pointBalance()).isEqualTo(10000); // 차감되지 않음
            verify(orderServiceClient, never()).getOrder(any(), any());
            verify(memberPointHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("사용자 실수 등에 의한 중복 차감 요청은 주문 검사로 중복 처리하지 않는다")
        void deductPoints_duplicate() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            MemberPoint memberPoint = new MemberPoint(memberId);
            memberPoint.addPoints(10000);

            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
            when(memberPointHistoryRepository.existsByOrderIdAndStatus(orderId, MemberPointHistoryStatus.USED)).thenReturn(true);

            // when
            MemberPointInfo result = memberPointService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.pointBalance()).isEqualTo(10000); // 차감되지 않음
            verify(orderServiceClient, never()).getOrder(any(), any());
            verify(memberPointHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 차감 시 예외가 발생한다")
        void deductPoints_fail_whenMemberNotFound() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, UUID.randomUUID(), 5000);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberPointService.deductPoints(memberId, command))
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

            MemberPoint memberPoint = new MemberPoint(memberId);
            memberPoint.addPoints(5000);

            PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, 3000);
            MemberPointHistory history = MemberPointHistory.returned(memberId, command);
            OrderInfo orderInfo = new OrderInfo(orderId, 3000, OrderInfo.Status.SUCCESS, memberId, 1);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
            when(memberPointHistoryRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
            when(memberPointHistoryRepository.save(any(MemberPointHistory.class))).thenReturn(history);
            when(orderServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

            // when
            MemberPointInfo result = memberPointService.returnPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.pointBalance()).isEqualTo(8000);
            verify(applicationEventPublisher).publishEvent(any(PointReturnedEvent.class));
        }

        @Test
        @DisplayName("이미 처리된 반환 요청은 멱등성 키로 중복 처리하지 않는다")
        void returnPoints_idempotent() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();

            MemberPoint memberPoint = new MemberPoint(memberId);
            memberPoint.addPoints(5000);

            PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, 3000);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
            when(memberPointHistoryRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

            // when
            MemberPointInfo result = memberPointService.returnPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.pointBalance()).isEqualTo(5000); // 반환되지 않음
            verify(orderServiceClient, never()).getOrder(any(), any());
            verify(memberPointHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 반환 시 예외가 발생한다")
        void returnPoints_fail_whenMemberNotFound() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID idempotencyKey = UUID.randomUUID();
            PointReturnCommand command = new PointReturnCommand(idempotencyKey, UUID.randomUUID(), 3000);

            when(memberPointRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberPointService.returnPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
