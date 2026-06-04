package store._0982.batch.application.sellerpayout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.batch.domain.sellerpayout.SellerPayoutFailureRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutFailure;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerPayoutService 단위 테스트")
class SellerPayoutServiceTest {

    @Mock
    private SellerPayoutFailureRepository sellerPayoutFailureRepository;

    private SellerPayoutService service;

    @BeforeEach
    void setUp() {
        service = new SellerPayoutService(sellerPayoutFailureRepository);
    }

    private SellerPayout createPayout(UUID sellerId, long amount) {
        return SellerPayout.createSellerPayout(
                sellerId,
                OffsetDateTime.now().minusMonths(1),
                OffsetDateTime.now(),
                amount,
                null,
                null
        );
    }

    @Nested
    @DisplayName("정상 처리")
    class NormalCase {

        @Test
        @DisplayName("실패 사유가 저장된다")
        void saveSellerPayoutFailure_shouldSaveFailureWithReason() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            String reason = "계좌 정보 없음";

            // when
            service.saveSellerPayoutFailure(payout, reason);

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getFailureReason().equals(reason)
            ));
        }

        @Test
        @DisplayName("실패 기록의 sellerId가 payout의 sellerId와 일치한다")
        void saveSellerPayoutFailure_shouldSaveWithCorrectSellerId() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            // when
            service.saveSellerPayoutFailure(payout, "오류");

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getSellerId().equals(sellerId)
            ));
        }

        @Test
        @DisplayName("실패 기록의 sellerPayoutId가 payout의 sellerPayoutId와 일치한다")
        void saveSellerPayoutFailure_shouldSaveWithCorrectSellerPayoutId() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            // when
            service.saveSellerPayoutFailure(payout, "오류");

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getSellerPayoutId().equals(payout.getSellerPayoutId())
            ));
        }

        @Test
        @DisplayName("실패 기록의 periodStart와 periodEnd가 payout과 일치한다")
        void saveSellerPayoutFailure_shouldSaveWithCorrectPeriod() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            // when
            service.saveSellerPayoutFailure(payout, "오류");

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getPeriodStart().equals(payout.getPeriodStart())
                    && failure.getPeriodEnd().equals(payout.getPeriodEnd())
            ));
        }

        @Test
        @DisplayName("실패 기록의 retryCount는 0으로 초기화된다")
        void saveSellerPayoutFailure_shouldSaveWithZeroRetryCount() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            // when
            service.saveSellerPayoutFailure(payout, "오류");

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getRetryCount() == 0
            ));
        }

        @Test
        @DisplayName("save가 정확히 1번 호출된다")
        void saveSellerPayoutFailure_shouldCallSaveOnce() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            // when
            service.saveSellerPayoutFailure(payout, "오류");

            // then
            verify(sellerPayoutFailureRepository, times(1)).save(any(SellerPayoutFailure.class));
        }
    }

    @Nested
    @DisplayName("실패 사유 경계 값")
    class FailureReasonCase {

        @Test
        @DisplayName("빈 문자열 실패 사유도 저장된다")
        void saveSellerPayoutFailure_shouldSaveWithEmptyReason() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 10_000L);

            // when
            service.saveSellerPayoutFailure(payout, "");

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getFailureReason().isEmpty()
            ));
        }

        @Test
        @DisplayName("긴 실패 사유도 저장된다")
        void saveSellerPayoutFailure_shouldSaveWithLongReason() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 10_000L);
            String longReason = "A".repeat(500);

            // when
            service.saveSellerPayoutFailure(payout, longReason);

            // then
            verify(sellerPayoutFailureRepository).save(argThat(failure ->
                    failure.getFailureReason().equals(longReason)
            ));
        }
    }
}
