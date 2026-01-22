package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointPaymentServiceTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @InjectMocks
    private PointPaymentService pointPaymentService;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
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
            PointBalanceInfo result = pointPaymentService.getPoints(memberId);

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
            assertThatThrownBy(() -> pointPaymentService.getPoints(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
