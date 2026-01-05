package store._0982.commerce.integration.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.presentation.product.dto.ProductRegisterRequest;
import store._0982.common.HeaderName;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.ProductEvent;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product 통합 테스트")
class ProductIntegrationTest {

    @MockitoBean
    private KafkaTemplate<String, ProductEvent> productKafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();
    }

    @Test
    @DisplayName("상품을 생성합니다.")
    void createProduct_success() throws Exception {
        // given
        ProductRegisterRequest request = new ProductRegisterRequest(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product"
        );

        // when & then - HTTP 응답 검증
        mockMvc.perform(
                        post("/api/products")
                                .header(HeaderName.ID, testMemberId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("상품이 등록되었습니다."))
                .andExpect(jsonPath("$.data.productId").exists())
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.category").value("BEAUTY"))
                .andExpect(jsonPath("$.data.description").value("테스트 상품 설명"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andExpect(jsonPath("$.data.originalUrl").value("https://example.com/product"))
                .andExpect(jsonPath("$.data.sellerId").value(testMemberId.toString()))
                .andExpect(jsonPath("$.data.createdAt").exists());

        // then - DB 저장 검증
        Product savedProduct = productRepository.findBySellerId(testMemberId, PageRequest.of(0, 10))
                .getContent()
                .get(0);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("테스트 상품");
        assertThat(savedProduct.getPrice()).isEqualTo(10000L);
        assertThat(savedProduct.getCategory()).isEqualTo(ProductCategory.BEAUTY);
        assertThat(savedProduct.getDescription()).isEqualTo("테스트 상품 설명");
        assertThat(savedProduct.getStock()).isEqualTo(100);
        assertThat(savedProduct.getSellerId()).isEqualTo(testMemberId);

        // then - Kafka 이벤트 발행 검증
        verify(productKafkaTemplate).send(
                eq(KafkaTopics.PRODUCT_UPSERTED),
                eq(savedProduct.getProductId().toString()),
                any(ProductEvent.class)
        );
    }

    @Test
    @DisplayName("필수 필드가 누락되면 Validation 에러로 400을 반환한다")
    void createProduct_requiredField() throws Exception {
        // given - name이 null인 잘못된 요청
        String invalidRequest = """
                {
                    "price": 10000,
                    "category": "BEAUTY",
                    "description": "테스트 상품 설명",
                    "stock": 100,
                    "originalUrl": "https://example.com/product"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/api/products")
                                .header(HeaderName.ID, testMemberId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("헤더에 Member ID가 없으면 401 에러를 반환한다")
    void createProduct_missingMemberId() throws Exception {
        // given
        ProductRegisterRequest request = new ProductRegisterRequest(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product"
        );

        // when & then - Member ID 헤더 없이 요청
        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("originalUrl은 선택 필드이므로 null이어도 성공한다")
    void createProduct_success_withoutOriginalUrl() throws Exception {
        // given - originalUrl이 null
        ProductRegisterRequest request = new ProductRegisterRequest(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                null  // 선택 필드
        );

        // when & then
        mockMvc.perform(
                        post("/api/products")
                                .header(HeaderName.ID, testMemberId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.originalUrl").isEmpty());
    }

    @Test
    @DisplayName("공동구매에 사용되지 않은 상품은 하드 삭제된다")
    void deleteProduct_hardDelete_success() throws Exception {
        // given - 상품 생성
        Product product = new Product(
                "삭제할 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트용 상품",
                100,
                "https://example.com/product",
                testMemberId
        );
        Product savedProduct = productRepository.saveAndFlush(product);
        UUID productId = savedProduct.getProductId();

        // when & then - HTTP 응답 검증
        mockMvc.perform(
                        delete("/api/products/" + productId)
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."));

        // then - DB에서 하드 삭제 검증
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();

        // then - Kafka 이벤트 발행 검증
        verify(productKafkaTemplate).send(
                eq(KafkaTopics.PRODUCT_DELETED),
                eq(productId.toString()),
                any(ProductEvent.class)
        );
    }

    @Test
    @DisplayName("공동구매에 사용된 상품은 소프트 삭제된다")
    void deleteProduct_softDelete_success() throws Exception {
        // given - 상품 생성
        Product product = new Product(
                "소프트 삭제할 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트용 상품",
                100,
                "https://example.com/product",
                testMemberId
        );
        Product savedProduct = productRepository.save(product);

        // given - 공동구매 생성 (상품이 사용됨)
        GroupPurchase groupPurchase = new GroupPurchase(
                50,
                100,
                "테스트 공동구매",
                "공동구매 설명",
                5000L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(7),
                testMemberId,
                savedProduct.getProductId()
        );
        groupPurchaseRepository.save(groupPurchase);

        // when & then - HTTP 응답 검증
        mockMvc.perform(
                        delete("/api/products/" + savedProduct.getProductId())
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("상품이 삭제되었습니다."));

        // then - DB에서 소프트 삭제 검증
        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getProductId());
        assertThat(deletedProduct).isPresent();
        assertThat(deletedProduct.get().getDeletedAt()).isNotNull();

        // then - Kafka 이벤트 발행 검증
        verify(productKafkaTemplate).send(
                eq(KafkaTopics.PRODUCT_DELETED),
                eq(savedProduct.getProductId().toString()),
                any(ProductEvent.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 404 에러를 반환한다")
    void deleteProduct_notFound() throws Exception {
        // given
        UUID nonExistentProductId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/api/products/" + nonExistentProductId)
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 판매자의 상품 삭제 시 403 에러를 반환한다")
    void deleteProduct_forbidden() throws Exception {
        // given - 다른 판매자의 상품 생성
        UUID otherSellerId = UUID.randomUUID();
        Product product = new Product(
                "다른 판매자 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트용 상품",
                100,
                "https://example.com/product",
                otherSellerId
        );
        Product savedProduct = productRepository.save(product);

        // when & then
        mockMvc.perform(
                        delete("/api/products/" + savedProduct.getProductId())
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
