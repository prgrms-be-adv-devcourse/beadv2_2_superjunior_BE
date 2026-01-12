package store._0982.commerce.integration.grouppurchase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.common.HeaderName;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GroupPurchase 통합 테스트")
class GrouppurchaseIntegrationTest {

    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseEvent> groupPurchaseKafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();
    }

    @Test
    @DisplayName("공동구매를 상세 조회한다")
    void getGroupPurchaseDetail_success() throws Exception {
        // given
        Product product = new Product(
                "테스트 상품",
                10000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                "https://example.com/product",
                testMemberId
        );
        Product savedProduct = productRepository.saveAndFlush(product);

        OffsetDateTime startDate = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(7);
        GroupPurchase groupPurchase = new GroupPurchase(
                50,
                100,
                "테스트 공동구매",
                "공동구매 설명입니다",
                5000L,
                startDate,
                endDate,
                testMemberId,
                savedProduct.getProductId()
        );
        GroupPurchase savedGroupPurchase = groupPurchaseRepository.saveAndFlush(groupPurchase);

        // when & then - HTTP 응답 검증
        mockMvc.perform(
                        get("/api/purchases/" + savedGroupPurchase.getGroupPurchaseId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("공동구매가 상세 조회되었습니다."))
                .andExpect(jsonPath("$.data.groupPurchaseId").value(savedGroupPurchase.getGroupPurchaseId().toString()))
                .andExpect(jsonPath("$.data.minQuantity").value(50))
                .andExpect(jsonPath("$.data.maxQuantity").value(100))
                .andExpect(jsonPath("$.data.title").value("테스트 공동구매"))
                .andExpect(jsonPath("$.data.description").value("공동구매 설명입니다"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.discountedPrice").value(5000))
                .andExpect(jsonPath("$.data.currentQuantity").value(0))
                .andExpect(jsonPath("$.data.sellerId").value(testMemberId.toString()))
                .andExpect(jsonPath("$.data.productId").value(savedProduct.getProductId().toString()))
                .andExpect(jsonPath("$.data.originalUrl").value("https://example.com/product"))
                .andExpect(jsonPath("$.data.category").value("BEAUTY"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.startDate").exists())
                .andExpect(jsonPath("$.data.endDate").exists());
    }

    @Test
    @DisplayName("존재하지 않는 공동구매 조회 시 404 에러를 반환한다")
    void getGroupPurchaseDetail_notFound() throws Exception {
        // given
        UUID nonExistentPurchaseId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/api/purchases/" + nonExistentPurchaseId)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공동구매 상세 조회 시 잘못된 UUID 형식이면 400 에러를 반환한다")
    void getGroupPurchaseDetail_invalidUUID() throws Exception {
        // given - 잘못된 UUID 형식
        String invalidPurchaseId = "not-a-valid-uuid";

        // when & then
        mockMvc.perform(
                        get("/api/purchases/" + invalidPurchaseId)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공동구매 목록을 페이징하여 조회한다")
    void getGroupPurchaseList_success() throws Exception {
        // given - 상품 3개 생성
        Product product1 = productRepository.saveAndFlush(new Product(
                "상품1", 10000L, ProductCategory.BEAUTY, "설명1", 100, "url1", testMemberId));
        Product product2 = productRepository.saveAndFlush(new Product(
                "상품2", 20000L, ProductCategory.FASHION, "설명2", 200, "url2", testMemberId));
        Product product3 = productRepository.saveAndFlush(new Product(
                "상품3", 30000L, ProductCategory.FOOD, "설명3", 300, "url3", testMemberId));

        // given - 공동구매 3개 생성
        OffsetDateTime now = OffsetDateTime.now();
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                10, 50, "공동구매1", "설명1", 5000L,
                now.plusDays(1), now.plusDays(7), testMemberId, product1.getProductId()));
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                20, 60, "공동구매2", "설명2", 15000L,
                now.plusDays(2), now.plusDays(8), testMemberId, product2.getProductId()));
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                30, 70, "공동구매3", "설명3", 25000L,
                now.plusDays(3), now.plusDays(9), testMemberId, product3.getProductId()));

        // when & then - 첫 페이지 조회
        mockMvc.perform(
                        get("/api/purchases")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("공동구매 목록 조회되었습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.numberOfElements").value(2))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false));
    }

    @Test
    @DisplayName("빈 목록일 때 공동구매 목록 조회가 성공한다")
    void getGroupPurchaseList_empty() throws Exception {
        // given - 데이터 없음

        // when & then
        mockMvc.perform(
                        get("/api/purchases")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("판매자별 공동구매 목록을 페이징하여 조회한다")
    void getGroupPurchaseListBySeller_success() throws Exception {
        // given
        UUID seller1 = UUID.randomUUID();
        UUID seller2 = UUID.randomUUID();

        Product p1 = productRepository.saveAndFlush(new Product(
                "판매자1상품1", 10000L, ProductCategory.BEAUTY, "설명", 100, "url", seller1));
        Product p2 = productRepository.saveAndFlush(new Product(
                "판매자1상품2", 20000L, ProductCategory.FASHION, "설명", 200, "url", seller1));
        Product p3 = productRepository.saveAndFlush(new Product(
                "판매자2상품1", 30000L, ProductCategory.FOOD, "설명", 300, "url", seller2));

        OffsetDateTime now = OffsetDateTime.now();
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                10, 50, "판매자1공동구매1", "설명", 5000L,
                now.plusDays(1), now.plusDays(7), seller1, p1.getProductId()));
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                20, 60, "판매자1공동구매2", "설명", 15000L,
                now.plusDays(2), now.plusDays(8), seller1, p2.getProductId()));
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                30, 70, "판매자2공동구매1", "설명", 25000L,
                now.plusDays(3), now.plusDays(9), seller2, p3.getProductId()));

        // when & then
        mockMvc.perform(
                        get("/api/purchases/seller/" + seller1)
                                .param("page", "0")
                                .param("size", "10")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("공동구매 판매자별 목록 조회되었습니다"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[?(@.title == '판매자1공동구매1')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.title == '판매자1공동구매2')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.title == '판매자2공동구매1')]").doesNotExist());
    }

    @Test
    @DisplayName("판매자별 공동구매 목록 조회 시 해당 판매자의 공동구매가 없으면 빈 목록을 반환한다")
    void getGroupPurchaseListBySeller_empty() throws Exception {
        // given - 다른 판매자의 공동구매만 존재
        UUID otherSeller = UUID.randomUUID();
        UUID targetSeller = UUID.randomUUID();

        Product product = productRepository.saveAndFlush(new Product(
                "다른판매자상품", 10000L, ProductCategory.BEAUTY, "설명", 100, "url", otherSeller));

        OffsetDateTime now = OffsetDateTime.now();
        groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                10, 50, "다른판매자공동구매", "설명", 5000L,
                now.plusDays(1), now.plusDays(7), otherSeller, product.getProductId()));

        // when & then
        mockMvc.perform(
                        get("/api/purchases/seller/" + targetSeller)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("판매자별 공동구매 목록 조회 시 잘못된 UUID 형식이면 400 에러를 반환한다")
    void getGroupPurchaseListBySeller_invalidUUID() throws Exception {
        // given - 잘못된 UUID 형식
        String invalidSellerId = "invalid-uuid-format";

        // when & then
        mockMvc.perform(
                        get("/api/purchases/seller/" + invalidSellerId)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SCHEDULED 상태의 공동구매를 삭제한다")
    void deleteGroupPurchase_success() throws Exception {
        // given
        Product product = productRepository.saveAndFlush(new Product(
                "테스트 상품", 10000L, ProductCategory.BEAUTY, "설명", 100, "url", testMemberId));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                50, 100, "삭제할 공동구매", "설명",
                5000L, now.plusDays(1), now.plusDays(7),
                testMemberId, product.getProductId()));
        UUID purchaseId = groupPurchase.getGroupPurchaseId();

        // when & then - HTTP 응답 검증
        mockMvc.perform(
                        delete("/api/purchases/" + purchaseId)
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("공동구매가 삭제되었습니다"));

        // then - DB에서 삭제 검증
        assertThat(groupPurchaseRepository.findById(purchaseId)).isEmpty();

        // then - Kafka 이벤트 발행 검증
        verify(groupPurchaseKafkaTemplate).send(
                eq(KafkaTopics.GROUP_PURCHASE_CHANGED),
                eq(purchaseId.toString()),
                any(GroupPurchaseEvent.class)
        );
    }

    @Test
    @DisplayName("OPEN 상태의 공동구매는 삭제할 수 없다")
    void deleteGroupPurchase_openStatus() throws Exception {
        // given
        Product product = productRepository.saveAndFlush(new Product(
                "테스트 상품", 10000L, ProductCategory.BEAUTY, "설명", 100, "url", testMemberId));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = new GroupPurchase(
                50, 100, "OPEN 공동구매", "설명",
                5000L, now.minusDays(1), now.plusDays(7),
                testMemberId, product.getProductId());
        groupPurchase.updateStatus(store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus.OPEN);
        groupPurchaseRepository.saveAndFlush(groupPurchase);

        // when & then
        mockMvc.perform(
                        delete("/api/purchases/" + groupPurchase.getGroupPurchaseId())
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다른 판매자의 공동구매는 삭제할 수 없다")
    void deleteGroupPurchase_forbidden() throws Exception {
        // given - 다른 판매자의 상품 및 공동구매 생성
        UUID otherSeller = UUID.randomUUID();
        Product product = productRepository.saveAndFlush(new Product(
                "다른 판매자 상품", 10000L, ProductCategory.BEAUTY, "설명", 100, "url", otherSeller));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                50, 100, "다른 판매자 공동구매", "설명",
                5000L, now.plusDays(1), now.plusDays(7),
                otherSeller, product.getProductId()));

        // when & then
        mockMvc.perform(
                        delete("/api/purchases/" + groupPurchase.getGroupPurchaseId())
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 공동구매 삭제 시 404 에러를 반환한다")
    void deleteGroupPurchase_notFound() throws Exception {
        // given
        UUID nonExistentPurchaseId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/api/purchases/" + nonExistentPurchaseId)
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공동구매 삭제 시 헤더에 Member ID가 없으면 401 에러를 반환한다")
    void deleteGroupPurchase_missingMemberId() throws Exception {
        // given - 공동구매 생성
        Product product = productRepository.saveAndFlush(new Product(
                "테스트 상품", 10000L, ProductCategory.BEAUTY, "설명", 100, "url", testMemberId));

        OffsetDateTime now = OffsetDateTime.now();
        GroupPurchase groupPurchase = groupPurchaseRepository.saveAndFlush(new GroupPurchase(
                50, 100, "공동구매", "설명",
                5000L, now.plusDays(1), now.plusDays(7),
                testMemberId, product.getProductId()));

        // when & then
        mockMvc.perform(
                        delete("/api/purchases/" + groupPurchase.getGroupPurchaseId())
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("공동구매 삭제 시 잘못된 UUID 형식이면 400 에러를 반환한다")
    void deleteGroupPurchase_invalidUUID() throws Exception {
        // given - 잘못된 UUID 형식
        String invalidPurchaseId = "uuid";

        // when & then
        mockMvc.perform(
                        delete("/api/purchases/" + invalidPurchaseId)
                                .header(HeaderName.ID, testMemberId.toString())
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}
