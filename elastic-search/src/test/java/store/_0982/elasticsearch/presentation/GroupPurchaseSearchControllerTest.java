package store._0982.elasticsearch.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupPurchaseSearchController.class)
class GroupPurchaseSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupPurchaseSearchService groupPurchaseSearchService;

    @Test
    @DisplayName("공동구매 인덱스 생성 API 호출 성공")
    void createGroupPurchaseIndex_success() throws Exception {
        // given
        doNothing()
                .when(groupPurchaseSearchService)
                .createGroupPurchaseIndex();

        // when & then
        mockMvc.perform(put("/api/searches/purchase/index"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("인덱스 생성 완료"));
    }

    @Test
    @DisplayName("공동구매 인덱스 삭제 API 호출 성공")
    void deleteGroupPurchaseIndex_success() throws Exception {
        // given
        doNothing()
                .when(groupPurchaseSearchService)
                .deleteGroupPurchaseIndex();

        // when & then
        mockMvc.perform(delete("/api/searches/purchase/index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("인덱스 삭제 완료"));
    }

    @Test
    @DisplayName("모든 공동구매 문서 검색 성공 - keyword + status 포함")
    void searchAllGroupPurchaseDocument_success() throws Exception {
        // given
        String keyword = "테스트";
        String status = "OPEN";
        String category = "";

        GroupPurchaseDocumentInfo doc =
                new GroupPurchaseDocumentInfo(
                        "gp-1",                 // groupPurchaseId
                        "샘플판매자",            // sellerName
                        10,                     // minQuantity
                        100,                    // maxQuantity
                        "테스트공동구매",        // title
                        "공동구매 설명",         // description
                        1_000_000L,             // discountedPrice
                        "OPEN",                 // status
                        "2025-01-01",            // startDate
                        "2025-01-31",            // endDate
                        OffsetDateTime.now(),   // createdAt
                        OffsetDateTime.now(),   // updatedAt
                        5,                      // currentQuantity
                        null,
                        null                    // productEvent
                );

        Page<GroupPurchaseDocumentInfo> page =
                new PageImpl<>(
                        List.of(doc),
                        PageRequest.of(0, 10),
                        1
                );

        PageResponse<GroupPurchaseDocumentInfo> response =
                PageResponse.from(page);

        when(groupPurchaseSearchService.searchAllGroupPurchaseDocument(
                eq(keyword),
                eq(status),
                eq(category),
                any(Pageable.class)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/searches/purchase/search/all")
                        .param("keyword", keyword)
                        .param("status", status)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("문서 검색 완료."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].groupPurchaseId").value("gp-1"));
    }

    @Test
    @DisplayName("판매자의 공동구매 문서 검색 성공 - keyword 없이")
    void searchGroupPurchaseDocumentBySeller_withoutKeyword_success() throws Exception {
        // given
        String keyword = "";
        UUID sellerId = UUID.randomUUID();

        ProductEvent event = new ProductEvent(
                UUID.randomUUID(),
                "테스트상품",
                5L,
                "HOME",
                "description",
                5,
                "url",
                sellerId,
                OffsetDateTime.now().toString(),
                OffsetDateTime.now().toString()
        );

        GroupPurchaseDocumentInfo doc =
                new GroupPurchaseDocumentInfo(
                        "gp-1",                 // groupPurchaseId
                        "샘플판매자",            // sellerName
                        10,                     // minQuantity
                        100,                    // maxQuantity
                        "테스트공동구매",        // title
                        "공동구매 설명",         // description
                        1_000_000L,             // discountedPrice
                        "OPEN",                 // status
                        "2025-01-01",            // startDate
                        "2025-01-31",            // endDate
                        OffsetDateTime.now(),   // createdAt
                        OffsetDateTime.now(),   // updatedAt
                        5,                      // currentQuantity
                        null,
                        ProductDocumentEmbedded.from(event) // productEvent
                );

        Page<GroupPurchaseDocumentInfo> page =
                new PageImpl<>(
                        List.of(doc),
                        PageRequest.of(0, 10),
                        1
                );

        PageResponse<GroupPurchaseDocumentInfo> response =
                PageResponse.from(page);

        when(groupPurchaseSearchService.searchGroupPurchaseDocument(
                eq(keyword),
                isNull(),
                eq(sellerId),
                eq(""),
                any(Pageable.class)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/searches/purchase/search/seller")
                        .param("keyword", keyword)
                        .param("sellerId", sellerId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].groupPurchaseId").value("gp-1"));
    }
}
