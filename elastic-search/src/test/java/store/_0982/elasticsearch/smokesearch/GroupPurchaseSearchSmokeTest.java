package store._0982.elasticsearch.smokesearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import store._0982.common.dto.PageResponse;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("smoke")
@SpringBootTest(classes = {
        ElasticsearchTestContext.class
})
@ActiveProfiles("search")
@Testcontainers
class GroupPurchaseSearchSmokeTest {
    private static final String SELLER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String OTHER_SELLER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String CATEGORY = "KIDS";
    private static final String OTHER_CATEGORY = "FOOD";
    private static final String KEYWORD = "toy";
    private static final String STATUS = "OPEN";

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.elasticsearch.uris",
                () -> "http://" + es.getHttpHostAddress()
        );
    }

    @Container
    static ElasticsearchContainer es =
            new ElasticsearchContainer(
                    "docker.elastic.co/elasticsearch/elasticsearch:8.11.1"
            )
                    .withEnv("xpack.security.enabled", "false")
                    .withCommand(
                            "bash",
                            "-c",
                            "bin/elasticsearch-plugin install --batch analysis-nori && /usr/local/bin/docker-entrypoint.sh"
                    );

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private GroupPurchaseSearchService service;

    @BeforeEach
    void setUp() {
        service.deleteGroupPurchaseIndex();
        service.createGroupPurchaseIndex();

        operations.save(
                GroupPurchaseDocument.builder()
                        .groupPurchaseId("gp-1")
                        .sellerName("seller-a")
                        .minQuantity(1)
                        .maxQuantity(10)
                        .title("toy group")
                        .description("toy description")
                        .discountedPrice(1000L)
                        .status(STATUS)
                        .startDate("2025-01-01")
                        .endDate("2025-01-31")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .currentQuantity(5)
                        .discountRate(10L)
                        .productDocumentEmbedded(
                                new ProductDocumentEmbedded(
                                        "p-1",
                                        CATEGORY,
                                        2000L,
                                        "https://example.com/p-1",
                                        SELLER_ID
                                )
                        )
                        .build()
        );
        operations.save(
                GroupPurchaseDocument.builder()
                        .groupPurchaseId("gp-2")
                        .sellerName("seller-b")
                        .minQuantity(1)
                        .maxQuantity(10)
                        .title("toy group")
                        .description("toy description")
                        .discountedPrice(1000L)
                        .status(STATUS)
                        .startDate("2025-01-01")
                        .endDate("2025-01-31")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .currentQuantity(5)
                        .discountRate(10L)
                        .productDocumentEmbedded(
                                new ProductDocumentEmbedded(
                                        "p-2",
                                        CATEGORY,
                                        2000L,
                                        "https://example.com/p-2",
                                        OTHER_SELLER_ID
                                )
                        )
                        .build()
        );
        operations.save(
                GroupPurchaseDocument.builder()
                        .groupPurchaseId("gp-3")
                        .sellerName("seller-a")
                        .minQuantity(1)
                        .maxQuantity(10)
                        .title("other title")
                        .description("other description")
                        .discountedPrice(1000L)
                        .status(STATUS)
                        .startDate("2025-01-01")
                        .endDate("2025-01-31")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .currentQuantity(5)
                        .discountRate(10L)
                        .productDocumentEmbedded(
                                new ProductDocumentEmbedded(
                                        "p-3",
                                        CATEGORY,
                                        2000L,
                                        "https://example.com/p-3",
                                        SELLER_ID
                                )
                        )
                        .build()
        );
        operations.save(
                GroupPurchaseDocument.builder()
                        .groupPurchaseId("gp-4")
                        .sellerName("seller-a")
                        .minQuantity(1)
                        .maxQuantity(10)
                        .title("toy group")
                        .description("toy description")
                        .discountedPrice(1000L)
                        .status("CLOSED")
                        .startDate("2025-01-01")
                        .endDate("2025-01-31")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .currentQuantity(5)
                        .discountRate(10L)
                        .productDocumentEmbedded(
                                new ProductDocumentEmbedded(
                                        "p-4",
                                        OTHER_CATEGORY,
                                        2000L,
                                        "https://example.com/p-4",
                                        SELLER_ID
                                )
                        )
                        .build()
        );

        operations.indexOps(GroupPurchaseDocument.class).refresh();
    }

    @Test
    @DisplayName("공동구매 키워드·상태·카테고리 검색")
    void group_purchase_search_smoke_test() {
        PageResponse<GroupPurchaseDocumentInfo> result =
                service.searchGroupPurchaseDocument(
                        KEYWORD,
                        STATUS,
                        UUID.fromString(SELLER_ID),
                        CATEGORY,
                        PageRequest.of(0, 10)
                );

        assertThat(result.content()).hasSize(1);
        GroupPurchaseDocumentInfo content = result.content().get(0);
        assertThat(content.groupPurchaseId()).isEqualTo("gp-1");
        assertThat(content.title()).contains(KEYWORD);
        assertThat(content.status()).isEqualTo(STATUS);
        assertThat(content.productDocumentEmbedded().getSellerId()).isEqualTo(SELLER_ID);
        assertThat(content.productDocumentEmbedded().getCategory()).isEqualTo(CATEGORY);
    }

    @Test
    @DisplayName("공동구매 전체 검색")
    void group_purchase_search_all_smoke_test() {
        PageResponse<GroupPurchaseDocumentInfo> result =
                service.searchAllGroupPurchaseDocument(
                        "",
                        "",
                        "",
                        PageRequest.of(0, 10)
                );

        assertThat(result.content()).hasSize(4);
        assertThat(result.content())
                .extracting(GroupPurchaseDocumentInfo::groupPurchaseId)
                .containsExactlyInAnyOrder("gp-1", "gp-2", "gp-3", "gp-4");
    }
}
