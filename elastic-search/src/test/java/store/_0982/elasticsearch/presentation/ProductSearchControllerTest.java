package store._0982.elasticsearch.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.elasticsearch.application.ProductSearchService;
import store._0982.elasticsearch.application.dto.ProductDocumentInfo;
import store._0982.elasticsearch.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductSearchController.class)
class ProductSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductSearchService productSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 인덱스 생성 API 호출 성공")
    void createProductIndex_success() throws Exception {
        // given
        Mockito.doNothing()
                .when(productSearchService)
                .createProductIndex();

        // when & then
        mockMvc.perform(put("/api/searches/product/index"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("인덱스 생성 완료"));

        verify(productSearchService).createProductIndex();
    }

    @Test
    @DisplayName("이미 존재하는 상품 인덱스 생성 시 409 에러 반환")
    void createProductIndex_alreadyExists() throws Exception {
        // given
        doThrow(new CustomException(CustomErrorCode.ALREADY_EXIST_INDEX))
                .when(productSearchService)
                .createProductIndex();

        // when & then
        mockMvc.perform(put("/api/searches/product/index"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("이미 존재하는 인덱스입니다."));
    }

    @Test
    @DisplayName("상품 인덱스 삭제 API 호출 성공")
    void deleteProductIndex_success() throws Exception {
        // given
        Mockito.doNothing()
                .when(productSearchService)
                .deleteProductIndex();

        // when & then
        mockMvc.perform(delete("/api/searches/product/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("인덱스 삭제 완료"));

        verify(productSearchService).deleteProductIndex();
    }

    @Test
    @DisplayName("존재하지 않는 공동구매 인덱스 삭제 시 404 에러 반환")
    void deleteProductIndex_alreadyExists() throws Exception {
        // given
        doThrow(new CustomException(CustomErrorCode.DONOT_EXIST_INDEX))
                .when(productSearchService)
                .deleteProductIndex();

        // when & then
        mockMvc.perform(delete("/api/searches/product/index"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("삭제할 인덱스가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("상품 문서 검색 성공 - keyword, sellerId, category 포함")
    void searchProductDocument_success() throws Exception {
        // given
        UUID sellerId = UUID.randomUUID();
        String keyword = "아이폰";
        String category = "KIDS";

        ProductDocumentInfo doc = new ProductDocumentInfo(
                "1",
                "아이폰 테스트",
                1_200_000L,
                category,
                "아이폰 설명",
                10,
                "https://example.com/product/1",
                sellerId.toString(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        Page<ProductDocumentInfo> page = new PageImpl<>(
                List.of(doc),
                PageRequest.of(0, 10),
                1
        );

        PageResponse<ProductDocumentInfo> response =
                PageResponse.from(page);

        Mockito.when(productSearchService.searchProductDocument(
                        eq(keyword),
                        eq(sellerId),
                        eq(category),
                        any(Pageable.class)
                ))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/searches/product/search")
                        .param("keyword", keyword)
                        .param("sellerId", sellerId.toString())
                        .param("category", category)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("상품 문서 검색 완료."))
                .andExpect(jsonPath("$.data.content[0].name").value("아이폰 테스트"));
    }

    @Test
    @DisplayName("category 없이 상품 문서 검색 성공")
    void searchProductDocument_withoutCategory_success() throws Exception {
        // given
        UUID sellerId = UUID.randomUUID();
        String keyword = "test";

        Page<ProductDocumentInfo> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                1
        );

        PageResponse<ProductDocumentInfo> response =
                PageResponse.from(page);

        Mockito.when(productSearchService.searchProductDocument(
                        eq(keyword),
                        eq(sellerId),
                        isNull(),
                        any(Pageable.class)
                ))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/searches/product/search")
                        .param("keyword", keyword)
                        .param("sellerId", sellerId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
