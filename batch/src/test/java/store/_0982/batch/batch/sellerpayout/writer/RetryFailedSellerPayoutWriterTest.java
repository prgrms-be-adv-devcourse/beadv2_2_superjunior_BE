package store._0982.batch.batch.sellerpayout.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.batch.application.sellerbalance.SellerBalanceService;
import store._0982.batch.application.sellerpayout.BankTransferService;
import store._0982.batch.application.sellerpayout.event.SellerPayoutCompletedEvent;
import store._0982.batch.application.sellerpayout.event.SellerPayoutFailedEvent;
import store._0982.batch.batch.sellerpayout.dto.SellerAccountDto;
import store._0982.batch.domain.sellerpayout.SellerPayoutFailureRepository;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.batch.infrastructure.sellerpayout.SellerAccountJdbcRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryFailedSellerPayoutWriter 단위 테스트")
class RetryFailedSellerPayoutWriterTest {

    @Mock private SellerAccountJdbcRepository sellerAccountJdbcRepository;
    @Mock private SellerPayoutRepository sellerPayoutRepository;
    @Mock private SellerPayoutFailureRepository sellerPayoutFailureRepository;
    @Mock private BankTransferService bankTransferService;
    @Mock private SellerBalanceService sellerBalanceService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Captor private ArgumentCaptor<UUID> payoutIdCaptor;

    private RetryFailedSellerPayoutWriter writer;

    @BeforeEach
    void setUp() {
        writer = new RetryFailedSellerPayoutWriter(
                sellerAccountJdbcRepository,
                sellerPayoutRepository,
                sellerPayoutFailureRepository,
                bankTransferService,
                sellerBalanceService,
                eventPublisher
        );
    }

    private SellerPayout createFailedPayout(UUID sellerId, long amount) {
        SellerPayout payout = SellerPayout.createSellerPayout(
                sellerId,
                OffsetDateTime.now().minusMonths(1),
                OffsetDateTime.now(),
                amount,
                null,
                null
        );
        payout.markAsFailed();
        return payout;
    }

    private SellerAccountDto validAccount(UUID sellerId) {
        return new SellerAccountDto(sellerId, "004", "123456789", "테스트판매자");
    }

    @Nested
    @DisplayName("재시도 성공")
    class RetrySuccessCase {

        @Test
        @DisplayName("계좌 정보가 복구되면 COMPLETED로 변경하고 SellerPayoutFailure를 삭제한다")
        void write_shouldCompleteAndDeleteFailureRecord() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createFailedPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.COMPLETED);
            verify(sellerPayoutFailureRepository).deleteBySellerPayoutId(payout.getSellerPayoutId());
            verify(sellerPayoutFailureRepository, never()).incrementRetryCount(any());
        }

        @Test
        @DisplayName("재시도 성공 시 SellerPayoutCompletedEvent를 발행한다")
        void write_shouldPublishCompletedEventOnSuccess() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createFailedPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(eventPublisher).publishEvent(any(SellerPayoutCompletedEvent.class));
            verify(eventPublisher, never()).publishEvent(any(SellerPayoutFailedEvent.class));
        }

        @Test
        @DisplayName("재시도 성공 시 clearBalance가 호출된다")
        void write_shouldClearBalanceOnSuccess() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createFailedPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(sellerBalanceService).clearBalance(payout);
        }
    }

    @Nested
    @DisplayName("재시도 실패")
    class RetryFailCase {

        @Test
        @DisplayName("계좌 정보가 여전히 없으면 retryCount를 증가시킨다")
        void write_shouldIncrementRetryCountOnFailure() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createFailedPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of());

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(sellerPayoutFailureRepository).incrementRetryCount(payoutIdCaptor.capture());
            assertThat(payoutIdCaptor.getValue()).isEqualTo(payout.getSellerPayoutId());
            verify(sellerPayoutFailureRepository, never()).deleteBySellerPayoutId(any());
        }

        @Test
        @DisplayName("재시도 실패 시 SellerPayoutFailedEvent를 발행한다")
        void write_shouldPublishFailedEventOnFailure() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createFailedPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of());

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(eventPublisher).publishEvent(any(SellerPayoutFailedEvent.class));
            verify(eventPublisher, never()).publishEvent(any(SellerPayoutCompletedEvent.class));
        }

        @Test
        @DisplayName("재시도 실패 시 clearBalance가 호출되지 않는다")
        void write_shouldNotClearBalanceOnFailure() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createFailedPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of());

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(sellerBalanceService, never()).clearBalance(any());
        }

        @Test
        @DisplayName("일부 성공 일부 실패 시 각각 독립적으로 처리된다")
        void write_shouldHandleMixedResults() {
            // given
            UUID successSellerId = UUID.randomUUID();
            UUID failSellerId = UUID.randomUUID();
            SellerPayout successPayout = createFailedPayout(successSellerId, 50_000L);
            SellerPayout failPayout = createFailedPayout(failSellerId, 30_000L);

            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(successSellerId, validAccount(successSellerId)));

            // when
            writer.write(new Chunk<>(List.of(successPayout, failPayout)));

            // then
            assertThat(successPayout.getStatus()).isEqualTo(SellerPayoutStatus.COMPLETED);
            assertThat(failPayout.getStatus()).isEqualTo(SellerPayoutStatus.FAILED);
            verify(sellerPayoutFailureRepository).deleteBySellerPayoutId(successPayout.getSellerPayoutId());
            verify(sellerPayoutFailureRepository).incrementRetryCount(failPayout.getSellerPayoutId());
        }
    }
}
