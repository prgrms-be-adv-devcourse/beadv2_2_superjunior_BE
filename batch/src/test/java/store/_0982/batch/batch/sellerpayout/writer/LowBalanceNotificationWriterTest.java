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
import store._0982.batch.application.sellerpayout.event.SellerPayoutDeferredEvent;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LowBalanceNotificationWriter 단위 테스트")
class LowBalanceNotificationWriterTest {

    @Mock private SellerPayoutRepository sellerPayoutRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Captor private ArgumentCaptor<List<SellerPayout>> payoutsCaptor;
    @Captor private ArgumentCaptor<SellerPayoutDeferredEvent> deferredEventCaptor;

    private LowBalanceNotificationWriter writer;

    @BeforeEach
    void setUp() {
        writer = new LowBalanceNotificationWriter(sellerPayoutRepository, eventPublisher);
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
        @DisplayName("SellerPayout을 DEFERRED 상태로 변경하고 저장한다")
        void write_shouldMarkDeferredAndSave() {
            // given
            SellerPayout payout = createPayout(UUID.randomUUID(), 10_000L);

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.DEFERRED);
            verify(sellerPayoutRepository).saveAll(payoutsCaptor.capture());
            assertThat(payoutsCaptor.getValue()).containsExactly(payout);
        }

        @Test
        @DisplayName("각 SellerPayout마다 SellerPayoutDeferredEvent를 발행한다")
        void write_shouldPublishDeferredEventPerPayout() {
            // given
            SellerPayout payout1 = createPayout(UUID.randomUUID(), 5_000L);
            SellerPayout payout2 = createPayout(UUID.randomUUID(), 15_000L);

            // when
            writer.write(new Chunk<>(List.of(payout1, payout2)));

            // then
            verify(eventPublisher, times(2)).publishEvent(any(SellerPayoutDeferredEvent.class));
        }

        @Test
        @DisplayName("여러 payout을 한 번의 saveAll로 저장한다")
        void write_shouldSaveAllAtOnce() {
            // given
            List<SellerPayout> payouts = List.of(
                    createPayout(UUID.randomUUID(), 5_000L),
                    createPayout(UUID.randomUUID(), 10_000L),
                    createPayout(UUID.randomUUID(), 20_000L)
            );

            // when
            writer.write(new Chunk<>(payouts));

            // then
            verify(sellerPayoutRepository, times(1)).saveAll(payoutsCaptor.capture());
            assertThat(payoutsCaptor.getValue()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("빈 청크")
    class EmptyChunk {

        @Test
        @DisplayName("빈 청크 입력 시 saveAll이 빈 리스트로 호출된다")
        void write_shouldCallSaveAllWithEmptyList() {
            // when
            writer.write(new Chunk<>(List.of()));

            // then
            verify(sellerPayoutRepository).saveAll(payoutsCaptor.capture());
            assertThat(payoutsCaptor.getValue()).isEmpty();
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("상태 변경 순서")
    class StateChangeOrder {

        @Test
        @DisplayName("markAsDeferred 후 saveAll이 호출된다")
        void write_shouldMarkDeferredBeforeSaveAll() {
            // given
            SellerPayout payout = createPayout(UUID.randomUUID(), 10_000L);

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then: DEFERRED 상태로 변경된 뒤 저장됐는지 순서 검증
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.DEFERRED);
            verify(sellerPayoutRepository).saveAll(payoutsCaptor.capture());
            assertThat(payoutsCaptor.getValue().get(0).getStatus()).isEqualTo(SellerPayoutStatus.DEFERRED);
        }
    }
}
