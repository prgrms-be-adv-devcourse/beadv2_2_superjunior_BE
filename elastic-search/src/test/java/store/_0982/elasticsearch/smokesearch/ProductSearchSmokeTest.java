package store._0982.elasticsearch.smokesearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.elasticsearch.application.ProductSearchService;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.domain.ProductDocument;
import store._0982.elasticsearch.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("smoke")
@SpringBootTest(classes = {
        ElasticsearchTestContext.class
})
@ActiveProfiles("search")
@Testcontainers
class ProductSearchSmokeTest {
    private static final String SELLER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String OTHER_SELLER_ID = "22222222-2222-2222-2222-222222222222";
    private static final String CATEGORY = "KIDS";
    private static final String OTHER_CATEGORY = "FOOD";
    private static final String KEYWORD = "아이폰";
    private static final String OTHER_NAME = "not-match";

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
    private ProductSearchService service;

    @BeforeEach
    void setUp() {
        service.deleteProductIndex();
        service.createProductIndex();

        operations.save(
                ProductDocument.builder()
                        .productId("p-1")
                        .name(KEYWORD)
                        .category(CATEGORY)
                        .sellerId(SELLER_ID)
                        .build()
        );
        operations.save(
                ProductDocument.builder()
                        .productId("p-2")
                        .name(KEYWORD)
                        .category(CATEGORY)
                        .sellerId(OTHER_SELLER_ID)
                        .build()
        );
        operations.save(
                ProductDocument.builder()
                        .productId("p-3")
                        .name(OTHER_NAME)
                        .category(CATEGORY)
                        .sellerId(SELLER_ID)
                        .build()
        );
        operations.save(
                ProductDocument.builder()
                        .productId("p-4")
                        .name(KEYWORD)
                        .category(OTHER_CATEGORY)
                        .sellerId(SELLER_ID)
                        .build()
        );
        operations.indexOps(ProductDocument.class).refresh();
    }

    @Test
    @DisplayName("내 상품 키워드, 카테고리 기준 검색")
    void product_search_smoke_test() {
        PageResponse<ProductDocumentInfo> result =
                service.searchProductDocument(
                        KEYWORD, UUID.fromString(SELLER_ID), CATEGORY, PageRequest.of(0, 10)
                );

        assertThat(result.content()).hasSize(1);
        ProductDocumentInfo content = result.content().get(0);
        assertThat(content.productId()).isEqualTo("p-1");
        assertThat(content.name()).isEqualTo(KEYWORD);
        assertThat(content.category()).isEqualTo(CATEGORY);
        assertThat(content.sellerId()).isEqualTo(SELLER_ID);
    }

    @Test
    @DisplayName("내 상품 전체 검색")
    void product_search_all_smoke_test(){
        PageResponse<ProductDocumentInfo> result =
                service.searchProductDocument(
                        "", UUID.fromString(SELLER_ID), "", PageRequest.of(0, 10)
                );

        assertThat(result.content()).hasSize(3);

        assertThat(result.content())
                .extracting(ProductDocumentInfo::productId)
                .containsExactlyInAnyOrder("p-1", "p-3", "p-4");
    }

    @Test
    @DisplayName("sellerId가 null이면 SELLER_ID_ISNULL 에러가 발생한다")
    void product_search_fail_when_seller_id_is_null() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        assertThatThrownBy(() ->
                service.searchProductDocument(
                        KEYWORD,
                        null,
                        CATEGORY,
                        pageRequest
                )
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException e = (CustomException) exception;
                    assertThat(e.getErrorCode()).isEqualTo(CustomErrorCode.SELLER_ID_ISNULL);
                    assertThat(e.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getMessage()).contains("판매자 ID가 비어있습니다.");
                });
    }
}
