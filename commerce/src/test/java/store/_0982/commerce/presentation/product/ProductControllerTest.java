package store._0982.commerce.presentation.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.product.dto.ProductRegisterCommand;
import store._0982.commerce.application.product.dto.ProductRegisterInfo;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.presentation.product.dto.ProductRegisterRequest;
import store._0982.common.HeaderName;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        reset(productService);
    }

    @Nested
    @DisplayName("상품 등록 API")
    class CreateProductTest {

        @Test
        @DisplayName("상품을 등록합니다.")
        void createProduct_success() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg"
            );

            ProductRegisterInfo info = new ProductRegisterInfo(
                    productId,
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg",
                    memberId,
                    OffsetDateTime.now()
            );

            when(productService.createProduct(any(ProductRegisterCommand.class)))
                    .thenReturn(info);

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("상품이 등록되었습니다."))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                    .andExpect(jsonPath("$.data.price").value(10000))
                    .andExpect(jsonPath("$.data.category").value("FOOD"))
                    .andExpect(jsonPath("$.data.description").value("맛있는 테스트 상품입니다."))
                    .andExpect(jsonPath("$.data.stock").value(100))
                    .andExpect(jsonPath("$.data.originalUrl").value("https://example.com/image.jpg"))
                    .andExpect(jsonPath("$.data.sellerId").value(memberId.toString()));

            verify(productService, times(1)).createProduct(any(ProductRegisterCommand.class));
        }

        @Test
        @DisplayName("memberId 헤더가 없으면 401 에러가 발생합니다.")
        void createProduct_missingMemberId() throws Exception {
            // given
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("로그인 정보가 없습니다."));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("잘못된 UUID 형식이면 400 에러가 발생합니다.")
        void createProduct_invalidUUID() throws Exception {
            // given
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, "invalid-uuid")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("적절하지 않은 요청 값이 존재합니다."));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("상품명이 blank이면 400 에러가 발생합니다.")
        void createProduct_blankName() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("가격이 0 이하이면 400 에러가 발생합니다.")
        void createProduct_invalidPrice() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    -1000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("가격이 0 이면 400 에러가 발생합니다. (경계값)")
        void createProduct_zeroPrice() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    0L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    100,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("재고가 0 이하이면 400 에러가 발생합니다.")
        void createProduct_invalidStock() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    -10,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("재고가 0 이면 400 에러가 발생합니다. (경계값)")
        void createProduct_zeroStock() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "맛있는 테스트 상품입니다.",
                    0,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("설명이 blank이면 400 에러가 발생합니다.")
        void createProduct_blankDescription() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            ProductRegisterRequest request = new ProductRegisterRequest(
                    "테스트 상품",
                    10000L,
                    ProductCategory.FOOD,
                    "",
                    100,
                    "https://example.com/image.jpg"
            );

            // when & then
            mockMvc.perform(
                            post("/api/products")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(productService, never()).createProduct(any());
        }
    }
}
