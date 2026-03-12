package store._0982.recommendation.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.dto.ProductUpsertedEvent;
import store._0982.recommendation.application.dto.VectorSearchResponse;
import store._0982.recommendation.infrastructure.feign.search.dto.GroupPurchaseSearchInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotIndexService {

    private final VectorStore vectorStore;

    /**
     * 공동구매 상품 1건을 VectorStore에 색인
     * - Kafka 이벤트 수신 시 또는 수동 호출
     */
    public void indexOne(VectorSearchResponse gp) {
        Document doc = toDocument(gp);
        vectorStore.add(List.of(doc));
        log.info("chatbot indexed: gpId={}", gp.groupPurchaseId());
    }

    /**
     * 여러 건 일괄 색인
     */
    public int indexAll(List<VectorSearchResponse> groupPurchases) {
        if (groupPurchases == null || groupPurchases.isEmpty()) return 0;

        // 기존 문서 삭제 후 재색인
        List<String> ids = groupPurchases.stream()
                .map(gp -> gp.groupPurchaseId().toString())
                .toList();
        vectorStore.delete(ids);

        List<Document> docs = groupPurchases.stream()
                .map(this::toDocument)
                .toList();
        vectorStore.add(docs);

        log.info("chatbot indexed {} documents", docs.size());
        return docs.size();
    }

    /**
     * 재색인 (삭제 후 다시 추가)
     */
    public void reindex(VectorSearchResponse gp) {
        vectorStore.delete(List.of(gp.groupPurchaseId().toString()));
        vectorStore.add(List.of(toDocument(gp)));
    }

    /**
     * VectorSearchResponse → Spring AI Document 변환
     * dev_high_BE의 ProductRecommendService.toDocument() 참고
     */
    private Document toDocument(VectorSearchResponse gp) {
        // content: LLM이 읽을 텍스트 (임베딩 대상)
        String content = """
                상품명: %s
                설명: %s
                가격: %s원
                할인율: %s%%
                상태: %s
                """.formatted(
                nullToEmpty(gp.title()),
                nullToEmpty(gp.description()),
                gp.discountedPrice() != null ? gp.discountedPrice().toString() : "",
                gp.discountRate() != null ? gp.discountRate().toString() : "",
                nullToEmpty(gp.status())
        );

        // metadata: 검색 후 필터링/응답에 사용할 정보
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("groupPurchaseId", gp.groupPurchaseId().toString());
        metadata.put("title", nullToEmpty(gp.title()));
        metadata.put("description", nullToEmpty(gp.description()));
        metadata.put("discountedPrice", gp.discountedPrice() != null ? gp.discountedPrice() : 0);
        metadata.put("discountRate", gp.discountRate() != null ? gp.discountRate() : 0);
        metadata.put("status", nullToEmpty(gp.status()));
        metadata.put("imageUrl", nullToEmpty(gp.imageUrl()));
        metadata.put("minQuantity", gp.minQuantity() != null ? gp.minQuantity() : 0);
        metadata.put("currentQuantity", gp.currentQuantity() != null ? gp.currentQuantity() : 0);
        metadata.put("startDate", gp.startDate());
        metadata.put("endDate", gp.endDate());
        metadata.put("productId", gp.productSearchInfo() != null ? gp.productSearchInfo().productId() : "");
        metadata.put("category", gp.productSearchInfo() != null ? gp.productSearchInfo().category() : "");
        metadata.put("originalPrice", gp.productSearchInfo() != null ? gp.productSearchInfo().price() : 0);

        return new Document(
                gp.groupPurchaseId().toString(),  // id
                content,                           // 임베딩 대상 텍스트
                metadata                           // 메타데이터
        );
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public void indexFromEvent(ProductUpsertedEvent event) {
        String content = """
            상품명: %s
            설명: %s
            카테고리: %s
            """.formatted(
                nullToEmpty(event.getName()),
                nullToEmpty(event.getDescription()),
                event.getCategory() != null ? event.getCategory().name() : ""
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("groupPurchaseId", event.getProductId().toString());
        metadata.put("title", nullToEmpty(event.getName()));
        metadata.put("description", nullToEmpty(event.getDescription()));
        metadata.put("category", event.getCategory() != null ? event.getCategory().name() : "");
        metadata.put("status", "OPEN");  // 신규 등록은 OPEN으로 가정

        Document doc = new Document(
                event.getProductId().toString(),
                content,
                metadata
        );

        vectorStore.add(List.of(doc));
        log.info("chatbot indexed from event: productId={}", event.getProductId());
    }

    /**
     * GroupPurchaseSearchInfo 리스트로 일괄 색인
     * - searchByKeyword() 응답 타입에 대응
     * - 가격, 할인율, 상태 등 전체 정보 포함
     */
    public int indexAllFromSearchInfo(List<GroupPurchaseSearchInfo> groupPurchases) {
        if (groupPurchases == null || groupPurchases.isEmpty()) return 0;

        List<String> ids = groupPurchases.stream()
                .map(GroupPurchaseSearchInfo::groupPurchaseId)
                .toList();
        vectorStore.delete(ids);

        List<Document> docs = groupPurchases.stream()
                .map(this::toDocumentFromSearchInfo)
                .toList();
        vectorStore.add(docs);

        log.info("chatbot indexed {} documents from search info", docs.size());
        return docs.size();
    }

    private Document toDocumentFromSearchInfo(GroupPurchaseSearchInfo gp) {
        String content = """
            상품명: %s
            설명: %s
            가격: %s원
            할인율: %s%%
            상태: %s
            """.formatted(
                nullToEmpty(gp.title()),
                nullToEmpty(gp.description()),
                gp.discountedPrice() != null ? gp.discountedPrice().toString() : "",
                gp.discountRate() != null ? gp.discountRate().toString() : "",
                nullToEmpty(gp.status())
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("groupPurchaseId", nullToEmpty(gp.groupPurchaseId()));
        metadata.put("title", nullToEmpty(gp.title()));
        metadata.put("description", nullToEmpty(gp.description()));
        metadata.put("discountedPrice", gp.discountedPrice() != null ? gp.discountedPrice() : 0);
        metadata.put("discountRate", gp.discountRate() != null ? gp.discountRate() : 0);
        metadata.put("status", nullToEmpty(gp.status()));
        metadata.put("imageUrl", nullToEmpty(gp.imageUrl()));
        metadata.put("minQuantity", gp.minQuantity() != null ? gp.minQuantity() : 0);
        metadata.put("currentQuantity", gp.currentQuantity() != null ? gp.currentQuantity() : 0);
        metadata.put("startDate", gp.startDate());
        metadata.put("endDate", gp.endDate());
        metadata.put("productId", gp.productSearchInfo() != null ? gp.productSearchInfo().productId() : "");
        metadata.put("category", gp.productSearchInfo() != null ? gp.productSearchInfo().category() : "");
        metadata.put("originalPrice", gp.productSearchInfo() != null ? gp.productSearchInfo().price() : 0);

        return new Document(
                gp.groupPurchaseId(),
                content,
                metadata
        );
    }
}
