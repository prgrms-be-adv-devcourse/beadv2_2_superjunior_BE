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
@DisplayName("SellerPayoutWriter 단위 테스트")
class SellerPayoutWriterTest {

    @Mock private SellerAccountJdbcRepository sellerAccountJdbcRepository;
    @Mock private SellerPayoutRepository sellerPayoutRepository;
    @Mock private SellerPayoutFailureRepository sellerPayoutFailureRepository;
    @Mock private BankTransferService bankTransferService;
    @Mock private SellerBalanceService sellerBalanceService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Captor private ArgumentCaptor<List<SellerPayout>> payoutsCaptor;
    @Captor private ArgumentCaptor<List<store._0982.common.domain.sellerpayout.SellerPayoutFailure>> failuresCaptor;
    @Captor private ArgumentCaptor<SellerPayoutCompletedEvent> completedEventCaptor;

    private SellerPayoutWriter writer;

    @BeforeEach
    void setUp() {
        writer = new SellerPayoutWriter(
                sellerAccountJdbcRepository,
                sellerPayoutRepository,
                sellerPayoutFailureRepository,
                bankTransferService,
                sellerBalanceService,
                eventPublisher
        );
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

    private SellerAccountDto validAccount(UUID sellerId) {
        return new SellerAccountDto(sellerId, "004", "123456789", "테스트판매자");
    }

    @Nested
    @DisplayName("정상 처리")
    class NormalCase {

        @Test
        @DisplayName("유효한 계좌가 있으면 COMPLETED로 처리하고 이벤트를 발행한다")
        void write_shouldMarkCompletedAndPublishEvent() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.COMPLETED);
            verify(eventPublisher).publishEvent(any(SellerPayoutCompletedEvent.class));
        }

        @Test
        @DisplayName("COMPLETED 처리 후 SellerBalanceService.clearBalance가 호출된다")
        void write_shouldClearBalanceOnSuccess() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(sellerBalanceService).clearBalance(payout);
        }

        @Test
        @DisplayName("계좌 정보가 payout에 설정된다")
        void write_shouldSetAccountInfoOnPayout() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getAccountNumber()).isEqualTo("123456789");
            assertThat(payout.getBankCode()).isEqualTo("004");
        }

        @Test
        @DisplayName("여러 판매자를 한 번에 처리한다")
        void write_shouldProcessMultipleSellers() {
            // given
            UUID sellerId1 = UUID.randomUUID();
            UUID sellerId2 = UUID.randomUUID();
            SellerPayout payout1 = createPayout(sellerId1, 30_000L);
            SellerPayout payout2 = createPayout(sellerId2, 60_000L);

            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(
                            sellerId1, validAccount(sellerId1),
                            sellerId2, validAccount(sellerId2)
                    ));

            // when
            writer.write(new Chunk<>(List.of(payout1, payout2)));

            // then
            assertThat(payout1.getStatus()).isEqualTo(SellerPayoutStatus.COMPLETED);
            assertThat(payout2.getStatus()).isEqualTo(SellerPayoutStatus.COMPLETED);
            verify(sellerPayoutRepository).saveAll(payoutsCaptor.capture());
            assertThat(payoutsCaptor.getValue()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("계좌 정보 없음")
    class InvalidAccountCase {

        @Test
        @DisplayName("계좌 정보가 없으면 FAILED로 처리하고 SellerPayoutFailure를 저장한다")
        void write_shouldMarkFailedAndSaveFailureWhenNoAccount() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of()); // 계좌 없음

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.FAILED);
            verify(sellerPayoutFailureRepository).saveAll(failuresCaptor.capture());
            assertThat(failuresCaptor.getValue()).hasSize(1);
        }

        @Test
        @DisplayName("계좌 정보가 없으면 clearBalance와 이벤트 발행이 호출되지 않는다")
        void write_shouldNotClearBalanceOrPublishEventOnFailure() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of());

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(sellerBalanceService, never()).clearBalance(any());
            verify(eventPublisher, never()).publishEvent(any(SellerPayoutCompletedEvent.class));
        }

        @Test
        @DisplayName("계좌 번호가 빈 문자열이면 FAILED로 처리한다")
        void write_shouldMarkFailedWhenAccountNumberIsBlank() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            SellerAccountDto blankAccount = new SellerAccountDto(sellerId, "004", "  ", "테스트판매자");
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, blankAccount));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.FAILED);
        }

        @Test
        @DisplayName("일부 판매자만 계좌가 없을 때 성공/실패가 각각 처리된다")
        void write_shouldHandleMixedAccountAvailability() {
            // given
            UUID validSellerId = UUID.randomUUID();
            UUID invalidSellerId = UUID.randomUUID();
            SellerPayout validPayout = createPayout(validSellerId, 50_000L);
            SellerPayout invalidPayout = createPayout(invalidSellerId, 30_000L);

            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(validSellerId, validAccount(validSellerId)));

            // when
            writer.write(new Chunk<>(List.of(validPayout, invalidPayout)));

            // then
            assertThat(validPayout.getStatus()).isEqualTo(SellerPayoutStatus.COMPLETED);
            assertThat(invalidPayout.getStatus()).isEqualTo(SellerPayoutStatus.FAILED);
            verify(sellerPayoutFailureRepository).saveAll(failuresCaptor.capture());
            assertThat(failuresCaptor.getValue()).hasSize(1);
        }

        @Test
        @DisplayName("accountNumber가 null인 계좌이면 FAILED로 처리한다")
        void write_shouldMarkFailedWhenAccountNumberIsNull() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            SellerAccountDto nullNumberAccount = new SellerAccountDto(sellerId, "004", null, "테스트판매자");
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, nullNumberAccount));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            assertThat(payout.getStatus()).isEqualTo(SellerPayoutStatus.FAILED);
        }

        @Test
        @DisplayName("실패가 없으면 sellerPayoutFailureRepository.saveAll이 호출되지 않는다")
        void write_shouldNotSaveFailuresWhenAllSucceed() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);
            when(sellerAccountJdbcRepository.findAccountsBySellerIds(any()))
                    .thenReturn(Map.of(sellerId, validAccount(sellerId)));

            // when
            writer.write(new Chunk<>(List.of(payout)));

            // then
            verify(sellerPayoutFailureRepository, never()).saveAll(any());
        }
    }
}
