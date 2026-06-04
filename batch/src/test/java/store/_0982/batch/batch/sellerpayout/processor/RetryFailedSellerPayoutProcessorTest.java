package store._0982.batch.batch.sellerpayout.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.batch.batch.sellerpayout.dto.SellerPayoutFailureDto;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutStatus;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryFailedSellerPayoutProcessor 단위 테스트")
class RetryFailedSellerPayoutProcessorTest {

    @Mock
    private SellerPayoutRepository sellerPayoutRepository;

    private RetryFailedSellerPayoutProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new RetryFailedSellerPayoutProcessor(sellerPayoutRepository);
    }

    private SellerPayout createSellerPayout(UUID sellerId, SellerPayoutStatus status) {
        SellerPayout payout = SellerPayout.createSellerPayout(
                sellerId,
                OffsetDateTime.now().minusMonths(1),
                OffsetDateTime.now(),
                50_000L,
                null,
                null
        );
        if (status == SellerPayoutStatus.COMPLETED) payout.markAsCompleted();
        if (status == SellerPayoutStatus.FAILED) payout.markAsFailed();
        if (status == SellerPayoutStatus.DEFERRED) payout.markAsDeferred();
        return payout;
    }

    @Nested
    @DisplayName("정상 처리")
    class NormalCase {

        @Test
        @DisplayName("FAILED 상태의 SellerPayout을 반환한다")
        void process_shouldReturnFailedPayout() throws Exception {
            // given
            UUID payoutId = UUID.randomUUID();
            SellerPayout payout = createSellerPayout(UUID.randomUUID(), SellerPayoutStatus.FAILED);
            when(sellerPayoutRepository.findById(payoutId)).thenReturn(Optional.of(payout));

            // when
            SellerPayout result = processor.process(new SellerPayoutFailureDto(payoutId));

            // then
            assertThat(result).isEqualTo(payout);
        }

        @Test
        @DisplayName("PENDING 상태의 SellerPayout을 반환한다")
        void process_shouldReturnPendingPayout() throws Exception {
            // given
            UUID payoutId = UUID.randomUUID();
            SellerPayout payout = createSellerPayout(UUID.randomUUID(), SellerPayoutStatus.PENDING);
            when(sellerPayoutRepository.findById(payoutId)).thenReturn(Optional.of(payout));

            // when
            SellerPayout result = processor.process(new SellerPayoutFailureDto(payoutId));

            // then
            assertThat(result).isEqualTo(payout);
        }
    }

    @Nested
    @DisplayName("필터링")
    class FilterCase {

        @Test
        @DisplayName("SellerPayout이 존재하지 않으면 null을 반환한다")
        void process_shouldReturnNullWhenPayoutNotFound() throws Exception {
            // given
            UUID payoutId = UUID.randomUUID();
            when(sellerPayoutRepository.findById(payoutId)).thenReturn(Optional.empty());

            // when
            SellerPayout result = processor.process(new SellerPayoutFailureDto(payoutId));

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("이미 COMPLETED 상태이면 null을 반환한다")
        void process_shouldReturnNullWhenPayoutAlreadyCompleted() throws Exception {
            // given
            UUID payoutId = UUID.randomUUID();
            SellerPayout payout = createSellerPayout(UUID.randomUUID(), SellerPayoutStatus.COMPLETED);
            when(sellerPayoutRepository.findById(payoutId)).thenReturn(Optional.of(payout));

            // when
            SellerPayout result = processor.process(new SellerPayoutFailureDto(payoutId));

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("DEFERRED 상태이면 재시도 대상으로 반환한다")
        void process_shouldReturnDeferredPayout() throws Exception {
            // given
            UUID payoutId = UUID.randomUUID();
            SellerPayout payout = createSellerPayout(UUID.randomUUID(), SellerPayoutStatus.DEFERRED);
            when(sellerPayoutRepository.findById(payoutId)).thenReturn(Optional.of(payout));

            // when
            SellerPayout result = processor.process(new SellerPayoutFailureDto(payoutId));

            // then
            assertThat(result).isEqualTo(payout);
        }
    }
}
