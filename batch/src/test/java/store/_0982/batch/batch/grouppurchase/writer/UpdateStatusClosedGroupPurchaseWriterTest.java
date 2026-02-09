package store._0982.batch.batch.grouppurchase.writer;

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
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkFailedEvent;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkUpdateEvent;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateStatusClosedGroupPurchaseWriter 단위 테스트")
class UpdateStatusClosedGroupPurchaseWriterTest {

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<List<UUID>> idsCaptor;

    private UpdateStatusClosedGroupPurchaseWriter writer;

    @BeforeEach
    void setUp() {
        writer = new UpdateStatusClosedGroupPurchaseWriter(groupPurchaseRepository, eventPublisher);
    }

    @Nested
    @DisplayName("성공한 공동구매 처리")
    class SuccessfulGroupPurchase {

        @Test
        @DisplayName("성공한 공동구매는 bulkUpdateStatusWithSucceededAt을 호출한다")
        void write_withSuccessItems_shouldCallBulkUpdateStatusWithSucceededAt() {
            // given
            UUID successId1 = UUID.randomUUID();
            UUID successId2 = UUID.randomUUID();

            List<GroupPurchaseResultProjection> items = List.of(
                    createResultProjection(successId1, GroupPurchaseStatus.SUCCESS, true),
                    createResultProjection(successId2, GroupPurchaseStatus.SUCCESS, true)
            );
            Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

            // when
            writer.write(chunk);

            // then
            verify(groupPurchaseRepository).bulkUpdateStatusWithSucceededAt(
                    idsCaptor.capture(),
                    eq(GroupPurchaseStatus.SUCCESS)
            );
            assertThat(idsCaptor.getValue()).containsExactlyInAnyOrder(successId1, successId2);
        }

        @Test
        @DisplayName("성공한 공동구매만 있을 때 실패 이벤트는 발행하지 않는다")
        void write_withOnlySuccessItems_shouldNotPublishFailedEvent() {
            // given
            List<GroupPurchaseResultProjection> items = List.of(
                    createResultProjection(UUID.randomUUID(), GroupPurchaseStatus.SUCCESS, true)
            );
            Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

            // when
            writer.write(chunk);

            // then
            verify(eventPublisher, never()).publishEvent(any(GroupPurchaseChunkFailedEvent.class));
            verify(eventPublisher).publishEvent(any(GroupPurchaseChunkUpdateEvent.class));
        }
    }

    @Nested
    @DisplayName("실패한 공동구매 처리")
    class FailedGroupPurchase {

        @Test
        @DisplayName("실패한 공동구매는 bulkUpdateStatus를 호출한다")
        void write_withFailedItems_shouldCallBulkUpdateStatus() {
            // given
            UUID failedId1 = UUID.randomUUID();
            UUID failedId2 = UUID.randomUUID();

            List<GroupPurchaseResultProjection> items = List.of(
                    createResultProjection(failedId1, GroupPurchaseStatus.FAILED, false),
                    createResultProjection(failedId2, GroupPurchaseStatus.FAILED, false)
            );
            Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

            // when
            writer.write(chunk);

            // then
            verify(groupPurchaseRepository).bulkUpdateStatus(
                    idsCaptor.capture(),
                    eq(GroupPurchaseStatus.FAILED)
            );
            assertThat(idsCaptor.getValue()).containsExactlyInAnyOrder(failedId1, failedId2);
        }

        @Test
        @DisplayName("실패한 공동구매가 있으면 실패 이벤트를 발행한다")
        void write_withFailedItems_shouldPublishFailedEvent() {
            // given
            List<GroupPurchaseResultProjection> items = List.of(
                    createResultProjection(UUID.randomUUID(), GroupPurchaseStatus.FAILED, false)
            );
            Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

            // when
            writer.write(chunk);

            // then
            ArgumentCaptor<GroupPurchaseChunkFailedEvent> eventCaptor =
                    ArgumentCaptor.forClass(GroupPurchaseChunkFailedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().failedGroupPurchases()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("성공과 실패가 혼합된 경우")
    class MixedResults {

        @Test
        @DisplayName("성공과 실패가 혼합된 경우 각각 분리하여 처리한다")
        void write_withMixedItems_shouldProcessSeparately() {
            // given
            UUID successId = UUID.randomUUID();
            UUID failedId = UUID.randomUUID();

            List<GroupPurchaseResultProjection> items = List.of(
                    createResultProjection(successId, GroupPurchaseStatus.SUCCESS, true),
                    createResultProjection(failedId, GroupPurchaseStatus.FAILED, false)
            );
            Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

            // when
            writer.write(chunk);

            // then
            verify(groupPurchaseRepository).bulkUpdateStatusWithSucceededAt(anyList(), eq(GroupPurchaseStatus.SUCCESS));
            verify(groupPurchaseRepository).bulkUpdateStatus(anyList(), eq(GroupPurchaseStatus.FAILED));
        }

        @Test
        @DisplayName("성공과 실패가 혼합된 경우 모든 이벤트를 발행한다")
        void write_withMixedItems_shouldPublishAllEvents() {
            // given
            List<GroupPurchaseResultProjection> items = List.of(
                    createResultProjection(UUID.randomUUID(), GroupPurchaseStatus.SUCCESS, true),
                    createResultProjection(UUID.randomUUID(), GroupPurchaseStatus.FAILED, false)
            );
            Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

            // when
            writer.write(chunk);

            // then
            verify(eventPublisher).publishEvent(any(GroupPurchaseChunkFailedEvent.class));
            verify(eventPublisher).publishEvent(any(GroupPurchaseChunkUpdateEvent.class));
        }
    }

    @Test
    @DisplayName("빈 청크일 경우 벌크 업데이트를 호출하지 않는다")
    void write_withEmptyChunk_shouldNotCallBulkUpdate() {
        // given
        Chunk<GroupPurchaseResultProjection> emptyChunk = new Chunk<>(List.of());

        // when
        writer.write(emptyChunk);

        // then
        verify(groupPurchaseRepository, never()).bulkUpdateStatus(any(), any());
        verify(groupPurchaseRepository, never()).bulkUpdateStatusWithSucceededAt(any(), any());
    }

    @Test
    @DisplayName("빈 청크일 경우에도 업데이트 이벤트는 발행한다")
    void write_withEmptyChunk_shouldStillPublishUpdateEvent() {
        // given
        Chunk<GroupPurchaseResultProjection> emptyChunk = new Chunk<>(List.of());

        // when
        writer.write(emptyChunk);

        // then
        verify(eventPublisher).publishEvent(any(GroupPurchaseChunkUpdateEvent.class));
        verify(eventPublisher, never()).publishEvent(any(GroupPurchaseChunkFailedEvent.class));
    }

    private GroupPurchaseResultProjection createResultProjection(UUID groupPurchaseId,
                                                                   GroupPurchaseStatus status,
                                                                   boolean success) {
        return new GroupPurchaseResultProjection(
                groupPurchaseId,
                status,
                success ? 15 : 5,
                10,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now(),
                15000L,
                ProductCategory.FOOD,
                success
        );
    }
}
