package store._0982.batch.batch.grouppurchase.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenGroupPurchaseWriter 단위 테스트")
class OpenGroupPurchaseWriterTest {

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<List<UUID>> idsCaptor;

    @Captor
    private ArgumentCaptor<GroupPurchaseChunkUpdateEvent> eventCaptor;

    private OpenGroupPurchaseWriter writer;

    @BeforeEach
    void setUp() {
        writer = new OpenGroupPurchaseWriter(groupPurchaseRepository, eventPublisher);
    }

    @Test
    @DisplayName("청크 내 모든 공동구매를 OPEN 상태로 일괄 업데이트한다")
    void write_shouldBulkUpdateStatusToOpen() {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        List<GroupPurchaseResultProjection> items = List.of(
                createResultProjection(id1),
                createResultProjection(id2),
                createResultProjection(id3)
        );
        Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

        // when
        writer.write(chunk);

        // then
        verify(groupPurchaseRepository).bulkUpdateStatus(idsCaptor.capture(), eq(GroupPurchaseStatus.OPEN));
        List<UUID> capturedIds = idsCaptor.getValue();
        assertThat(capturedIds).containsExactlyInAnyOrder(id1, id2, id3);
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
    }

    @Test
    @DisplayName("청크 처리 후 이벤트를 발행한다")
    void write_shouldPublishEvent() {
        // given
        List<GroupPurchaseResultProjection> items = List.of(
                createResultProjection(UUID.randomUUID()),
                createResultProjection(UUID.randomUUID())
        );
        Chunk<GroupPurchaseResultProjection> chunk = new Chunk<>(items);

        // when
        writer.write(chunk);

        // then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        GroupPurchaseChunkUpdateEvent event = eventCaptor.getValue();
        assertThat(event.updatedItems()).hasSize(2);
    }

    @Test
    @DisplayName("빈 청크일 경우에도 이벤트는 발행한다")
    void write_withEmptyChunk_shouldStillPublishEvent() {
        // given
        Chunk<GroupPurchaseResultProjection> emptyChunk = new Chunk<>(List.of());

        // when
        writer.write(emptyChunk);

        // then
        verify(eventPublisher).publishEvent(any(GroupPurchaseChunkUpdateEvent.class));
    }

    private GroupPurchaseResultProjection createResultProjection(UUID groupPurchaseId) {
        return new GroupPurchaseResultProjection(
                groupPurchaseId,
                GroupPurchaseStatus.OPEN,
                0,
                10,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                OffsetDateTime.now().plusDays(7),
                OffsetDateTime.now(),
                15000L,
                ProductCategory.FOOD,
                true
        );
    }
}
