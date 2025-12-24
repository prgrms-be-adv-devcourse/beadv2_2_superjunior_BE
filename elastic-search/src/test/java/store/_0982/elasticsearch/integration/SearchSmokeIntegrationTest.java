package store._0982.elasticsearch.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import store._0982.common.dto.PageResponse;
import store._0982.elasticsearch.application.ProductSearchService;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.domain.ProductDocument;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class SearchSmokeIntegrationTest {

    @Container
    static ElasticsearchContainer es =
        new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch:8.11.1"
        ).withEnv("xpack.security.enabled", "false");

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private ProductSearchService service;

    @BeforeEach
    void setUp() {
        service.createProductIndex();

        operations.save(
            ProductDocument.builder()
                .productId("p-1")
                .name("아이폰 15")
                .category("KIDS")
                .sellerId("seller-1")
                .build()
        );
        operations.indexOps(ProductDocument.class).refresh();
    }

    @Test
    void search_smoke_test() {
        PageResponse<ProductDocumentInfo> result =
            service.searchProductDocument(
                "아이폰", UUID.fromString("seller-1"), "KIDS", PageRequest.of(0, 10)
            );

        assertThat(result.content()).hasSize(1);
    }
}
