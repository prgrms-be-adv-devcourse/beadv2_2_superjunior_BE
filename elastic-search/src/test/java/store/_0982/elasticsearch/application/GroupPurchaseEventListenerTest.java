package store._0982.elasticsearch.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.application.support.KafkaTestProbe;
import store._0982.elasticsearch.config.KafkaTestConfig;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;
import store._0982.elasticsearch.infrastructure.GroupPurchaseRepository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(KafkaTestConfig.class)
@ActiveProfiles("kafka")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                KafkaTopics.GROUP_PURCHASE_CREATED,
                KafkaTopics.GROUP_PURCHASE_CHANGED
        }
)
class GroupPurchaseEventListenerTest {

    @Autowired
    private KafkaTemplate<String, GroupPurchaseEvent> kafkaTemplate;

    @MockitoBean
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private KafkaTestProbe probe;

    @BeforeEach
    void setUp() {
        probe.reset();
    }

    private final ProductEvent productEvent = new ProductEvent( UUID.randomUUID(),
            "아이폰 15",
            1_200_000L,
            "KIDS",
            "아이폰 설명",
            10,
            "https://img.url",
            UUID.randomUUID(),
            "2025-01-01T00:00:00Z",
            "2025-01-01T00:00:00Z");

    @Test
    @DisplayName("GROUP_PURCHASE_CREATED 이벤트 수신 시 공동구매 문서 저장")
    void group_purchase_created_event_consumed_and_saved() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        GroupPurchaseEvent event = new GroupPurchaseEvent( id, // id
                1, // minQuantity
                10, // maxQuantity
                "공동구매 제목", // title
                "공동구매 설명", // description
                9_900L, // discountedPrice
                "OPEN", // status
                "판매자명", // sellerName
                "2025-01-01T00:00", // startDate
                "2025-01-10T00:00", // endDate
                "2025-01-01T00:00:00+09:00", // createdAt
                "2025-01-01T00:00:00+09:00", // updatedAt
                3, // currentQuantity p
                productEvent, // productEvent
                GroupPurchaseEvent.SearchKafkaStatus.CREATE_GROUP_PURCHASE );

        when(groupPurchaseRepository.save(any(GroupPurchaseDocument.class)))
                .thenAnswer(invocation -> {
                    probe.markConsumed();
                    return invocation.getArgument(0);
                });

        // when
        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CREATED, event).get();

        // then
        boolean consumed = probe.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        GroupPurchaseDocument saved = captureSavedGroupPurchaseDocument();
        assertGroupPurchaseDocument(saved, event);
        verify(groupPurchaseRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("GROUP_PURCHASE_CHANGED + DELETE 상태일 때 공동구매 문서 삭제")
    void group_purchase_changed_delete_event_consumed_and_deleted() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        GroupPurchaseEvent event = new GroupPurchaseEvent(
                id,                 // id
                1,
                10,
                "삭제될 공동구매",
                "삭제 설명",
                9_900L,
                "DELETED",
                "판매자명",
                "2025-01-01T00:00",
                "2025-01-10T00:00",
                "2025-01-01T00:00:00+09:00",
                "2025-01-01T00:00:00+09:00",
                0,
                productEvent,
                GroupPurchaseEvent.SearchKafkaStatus.DELETE_GROUP_PURCHASE
        );

        doAnswer(invocation -> {
            probe.markConsumed();
            return null;
        }).when(groupPurchaseRepository).deleteById(anyString());

        // when
        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CHANGED, event).get();

        // then
        boolean consumed = probe.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        verify(groupPurchaseRepository).deleteById(id.toString());
        verify(groupPurchaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("GROUP_PURCHASE_CHANGED + UPDATE 상태일 때 공동구매 문서 저장")
    void group_purchase_changed_update_event_consumed_and_saved() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        GroupPurchaseEvent event = new GroupPurchaseEvent(
                id,
                2,
                20,
                "수정된 공동구매",
                "수정 설명",
                12_000L,
                "OPEN",
                "판매자명",
                "2025-02-01T00:00",
                "2025-02-10T00:00",
                "2025-01-01T00:00:00+09:00",
                "2025-01-01T00:00:00+09:00",
                5,
                productEvent,
                GroupPurchaseEvent.SearchKafkaStatus.UPDATE_GROUP_PURCHASE
        );

        when(groupPurchaseRepository.save(any(GroupPurchaseDocument.class)))
                .thenAnswer(invocation -> {
                    probe.markConsumed();
                    return invocation.getArgument(0);
                });

        // when
        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CHANGED, event).get();

        // then
        boolean consumed = probe.await(10, TimeUnit.SECONDS);
        assertThat(consumed).isTrue();

        GroupPurchaseDocument saved = captureSavedGroupPurchaseDocument();
        assertGroupPurchaseDocument(saved, event);
        verify(groupPurchaseRepository, never()).deleteById(any());
    }

    private GroupPurchaseDocument captureSavedGroupPurchaseDocument() {
        ArgumentCaptor<GroupPurchaseDocument> captor =
                ArgumentCaptor.forClass(GroupPurchaseDocument.class);
        verify(groupPurchaseRepository).save(captor.capture());
        return captor.getValue();
    }

    private void assertGroupPurchaseDocument(GroupPurchaseDocument saved, GroupPurchaseEvent event) {
        assertThat(saved.getGroupPurchaseId()).isEqualTo(event.getId().toString());
        assertThat(saved.getSellerName()).isEqualTo(event.getSellerName());
        assertThat(saved.getMinQuantity()).isEqualTo(event.getMinQuantity());
        assertThat(saved.getMaxQuantity()).isEqualTo(event.getMaxQuantity());
        assertThat(saved.getTitle()).isEqualTo(event.getTitle());
        assertThat(saved.getDescription()).isEqualTo(event.getDescription());
        assertThat(saved.getDiscountedPrice()).isEqualTo(event.getDiscountedPrice());
        assertThat(saved.getStatus()).isEqualTo(event.getStatus());
        assertThat(saved.getStartDate()).isEqualTo(event.getStartDate());
        assertThat(saved.getEndDate()).isEqualTo(event.getEndDate());
        assertThat(saved.getCurrentQuantity()).isEqualTo(event.getCurrentQuantity());
        assertThat(saved.getCreatedAt()).isEqualTo(java.time.OffsetDateTime.parse(event.getCreatedAt()));
        assertThat(saved.getUpdatedAt()).isEqualTo(java.time.OffsetDateTime.parse(event.getUpdatedAt()));

        ProductDocumentEmbedded embedded = saved.getProductDocumentEmbedded();
        assertThat(embedded.getProductId()).isEqualTo(event.getProductEvent().getId().toString());
        assertThat(embedded.getCategory()).isEqualTo(event.getProductEvent().getCategory());
        assertThat(embedded.getPrice()).isEqualTo(event.getProductEvent().getPrice());
        assertThat(embedded.getOriginalUrl()).isEqualTo(event.getProductEvent().getOriginalUrl());
        assertThat(embedded.getSellerId()).isEqualTo(event.getProductEvent().getSellerId().toString());

        long expectedDiscountRate = Math.round(((double) (event.getProductEvent().getPrice() - event.getDiscountedPrice())
                / event.getProductEvent().getPrice()) * 100);
        assertThat(saved.getDiscountRate()).isEqualTo(expectedDiscountRate);
    }
}
