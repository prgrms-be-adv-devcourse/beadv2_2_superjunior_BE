package store._0982.elasticsearch.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.infrastructure.GroupPurchaseRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupPurchaseEventListenerTest {

    @Mock
    private GroupPurchaseRepository groupPurchaseRepository;

    @InjectMocks
    private GroupPurchaseEventListener listener;

    @Test
    @DisplayName("GROUP_PURCHASE_ADDED 이벤트 수신 시 문서 저장")
    void create_event_success() {
        // given
        UUID id = UUID.randomUUID();

        GroupPurchaseEvent event = new GroupPurchaseEvent(
                Clock.systemUTC(),
                id,
                10,
                100,
                "공동구매 생성",
                "설명",
                500_000L,
                "OPEN",
                "판매자",
                "2025-01-01",
                "2025-01-31",
                OffsetDateTime.now().toString(),
                OffsetDateTime.now().toString(),
                1,
                null,
                GroupPurchaseEvent.SearchKafkaStatus.CREATE_GROUP_PURCHASE
        );

        when(groupPurchaseRepository.save(any()))
                .thenReturn(mock(GroupPurchaseDocument.class));

        // when
        listener.create(event);

        // then
        verify(groupPurchaseRepository).save(any(GroupPurchaseDocument.class));
        verify(groupPurchaseRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("상태 변경 이벤트 - DELETE_GROUP_PURCHASE면 문서 삭제")
    void changed_event_delete() {
        // given
        UUID id = UUID.randomUUID();

        GroupPurchaseEvent event = new GroupPurchaseEvent(
                Clock.systemUTC(),
                id,
                null,
                null,
                null,
                null,
                1L,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                GroupPurchaseEvent.SearchKafkaStatus.DELETE_GROUP_PURCHASE
        );

        // when
        listener.changed(event);

        // then
        verify(groupPurchaseRepository).deleteById(id.toString());
        verify(groupPurchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("상태 변경 이벤트 - DELETE가 아니면 문서 저장")
    void changed_event_save() {
        // given
        UUID id = UUID.randomUUID();

        GroupPurchaseEvent event = new GroupPurchaseEvent(
                id,
                10,
                100,
                "아이폰 공동구매",
                "설명",
                1_000_000L,
                "OPEN",
                "애플스토어",
                "2025-01-01",
                "2025-01-31",
                OffsetDateTime.now().toString(),
                OffsetDateTime.now().toString(),
                5,
                null,
                GroupPurchaseEvent.SearchKafkaStatus.UPDATE_GROUP_PURCHASE
        );

        when(groupPurchaseRepository.save(any()))
                .thenReturn(mock(GroupPurchaseDocument.class));

        // when
        listener.changed(event);

        // then
        verify(groupPurchaseRepository).save(any(GroupPurchaseDocument.class));
        verify(groupPurchaseRepository, never()).deleteById(any());
    }
}
