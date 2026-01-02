package store._0982.commerce.presentation.grouppurchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.presentation.grouppurchase.dto.GroupPurchaseRegisterRequest;
import store._0982.commerce.presentation.grouppurchase.dto.GroupPurchaseUpdateRequest;
import store._0982.common.HeaderName;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupPurchaseController.class)
class GroupPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GroupPurchaseService groupPurchaseService;

    @Test
    @DisplayName("공동구매를 생성합니다.")
    void createGroupPurchase_success() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        GroupPurchaseRegisterRequest request = new GroupPurchaseRegisterRequest(
                10,100,"공동구매 제목","공동구매 설명", 2000L,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusDays(1),productId);

        GroupPurchaseInfo response = new GroupPurchaseInfo(UUID.randomUUID(), 10,100,2000L,
                "공동구매 제목", "공동구매 설명", GroupPurchaseStatus.SCHEDULED,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusDays(1),memberId, productId,OffsetDateTime.now(),OffsetDateTime.now());

        Mockito.when(groupPurchaseService.createGroupPurchase(
                Mockito.eq(memberId),
                Mockito.any()
        )).thenReturn(response);

        // when&then
        mockMvc.perform(
                post("/api/purchases")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("공동 구매가 생성 되었습니다."))
                .andExpect(jsonPath("$.data.title").value("공동구매 제목"));
    }

    @Test
    @DisplayName("공동구매를 수정합니다.")
    void updateGroupPurchase_success() throws Exception {
        // given
        UUID purchaseId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        GroupPurchaseUpdateRequest request = new GroupPurchaseUpdateRequest(
                10,100,"수정 제목", "수정 설명", 2000L,
                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusDays(1), UUID.randomUUID()
        );

        GroupPurchaseInfo response = Mockito.mock(GroupPurchaseInfo.class);

        Mockito.when(groupPurchaseService.updateGroupPurchase(
                Mockito.eq(memberId),
                Mockito.eq(purchaseId),
                Mockito.any()
        )).thenReturn(response);

        // when & then
        mockMvc.perform(
                        patch("/api/purchases/{purchaseId}", purchaseId)
                                .header(HeaderName.ID, memberId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("공동구매 정보가 수정되었습니다."));
    }

    @Test
    @DisplayName("공동구매 수정 - memberId 헤더 누락 시 400")
    void updateGroupPurchase_fail_noHeader() throws Exception {
        mockMvc.perform(
                        patch("/api/purchases/{purchaseId}", UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new GroupPurchaseUpdateRequest(10,100,"수정 제목", "수정 설명", 2000L,
                                                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusDays(1), UUID.randomUUID())
                                ))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("공동구매 수정 - validation 실패")
    void updateGroupPurchase_fail_validation() throws Exception {
        mockMvc.perform(
                        patch("/api/purchases/{purchaseId}", UUID.randomUUID())
                                .header(HeaderName.ID, UUID.randomUUID().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new GroupPurchaseUpdateRequest(0,0,"수정 제목", "수정 설명", 2000L,
                                                OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusDays(1), UUID.randomUUID())
                                ))
                )
                .andExpect(status().isBadRequest());
    }
}