package store._0982.elasticsearch.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import store._0982.common.exception.CustomException;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;

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

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("이미 존재하는 공동구매 인덱스 생성 시 409 에러 반환")
    void createGroupPurchaseIndex_alreadyExists() throws Exception {
        // given
        doThrow(new CustomException(CustomErrorCode.ALREADY_EXIST_INDEX))
                .when(groupPurchaseSearchService)
                .createGroupPurchaseIndex();

        // when & then
        mockMvc.perform(put("/api/searches/purchase/index"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("이미 존재하는 인덱스입니다."));
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
    @DisplayName("존재하지 않는 공동구매 인덱스 삭제 시 404 에러 반환")
    void deleteGroupPurchaseIndex_alreadyExists() throws Exception {
        // given
        doThrow(new CustomException(CustomErrorCode.DONOT_EXIST_INDEX))
                .when(groupPurchaseSearchService)
                .deleteGroupPurchaseIndex();

        // when & then
        mockMvc.perform(delete("/api/searches/purchase/index"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("삭제할 인덱스가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("공동구매 문서 검색 성공 - keyword + status 포함")
    void searchGroupPurchaseDocument_success() throws Exception {
        // given
        String keyword = "아이폰";
        String status = "OPEN";
        String category = "";

        GroupPurchaseDocumentInfo doc =
                new GroupPurchaseDocumentInfo(
                        "gp-1",                 // groupPurchaseId
                        "애플공식스토어",          // sellerName
                        10,                     // minQuantity
                        100,                    // maxQuantity
                        "아이폰 공동구매",          // title
                        "아이폰 공동구매 설명",     // description
                        1_000_000L,             // discountedPrice
                        "OPEN",                 // status
                        "2025-01-01",            // startDate
                        "2025-01-31",            // endDate
                        OffsetDateTime.now(),   // createdAt
                        OffsetDateTime.now(),   // updatedAt
                        5,                      // currentQuantity
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

        when(groupPurchaseSearchService.searchGroupPurchaseDocument(
                eq(keyword),
                eq(status),
                eq(category),
                any(Pageable.class)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/searches/purchase/search")
                        .param("keyword", keyword)
                        .param("status", status)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("문서 검색 완료."))
                .andExpect(jsonPath("$.data.content[0].title").value("아이폰 공동구매"));
    }

    @Test
    @DisplayName("status 없이 공동구매 문서 검색 성공")
    void searchGroupPurchaseDocument_withoutStatus_success() throws Exception {
        // given
        String keyword = "test";

        Page<GroupPurchaseDocumentInfo> page =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        PageResponse<GroupPurchaseDocumentInfo> response =
                PageResponse.from(page);

        when(groupPurchaseSearchService.searchGroupPurchaseDocument(
                eq(keyword),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/searches/purchase/search")
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
