package store._0982.commerce.presentation.grouppurchase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseDetailInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseThumbnailInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.common.dto.PageResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupPurchaseController.class)
class GroupPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupPurchaseService groupPurchaseService;

    @BeforeEach
    void setUp() {
        reset(groupPurchaseService);
    }

    @Nested
    @DisplayName("공동구매 상세 조회 API")
    class GetGroupPurchaseByIdTest {

        @Test
        @DisplayName("공동구매를 상세 조회합니다.")
        void getGroupPurchaseById_success() throws Exception {
            // given
            UUID purchaseId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();

            GroupPurchaseDetailInfo info = new GroupPurchaseDetailInfo(
                    purchaseId,
                    10,
                    100,
                    "테스트 공동구매",
                    "테스트 공동구매 설명입니다.",
                    20000L,
                    15000L,
                    50,
                    now.minusDays(1),
                    now.plusDays(7),
                    sellerId,
                    productId,
                    "https://example.com/image.jpg",
                    ProductCategory.FOOD,
                    GroupPurchaseStatus.OPEN,
                    now.minusDays(2)
            );

            when(groupPurchaseService.getGroupPurchaseById(purchaseId))
                    .thenReturn(info);

            // when & then
            mockMvc.perform(
                            get("/api/purchases/{purchaseId}", purchaseId)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공동구매가 상세 조회되었습니다."))
                    .andExpect(jsonPath("$.data.groupPurchaseId").value(purchaseId.toString()))
                    .andExpect(jsonPath("$.data.minQuantity").value(10))
                    .andExpect(jsonPath("$.data.maxQuantity").value(100))
                    .andExpect(jsonPath("$.data.title").value("테스트 공동구매"))
                    .andExpect(jsonPath("$.data.description").value("테스트 공동구매 설명입니다."))
                    .andExpect(jsonPath("$.data.price").value(20000))
                    .andExpect(jsonPath("$.data.discountedPrice").value(15000))
                    .andExpect(jsonPath("$.data.currentQuantity").value(50))
                    .andExpect(jsonPath("$.data.sellerId").value(sellerId.toString()))
                    .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                    .andExpect(jsonPath("$.data.originalUrl").value("https://example.com/image.jpg"))
                    .andExpect(jsonPath("$.data.category").value("FOOD"))
                    .andExpect(jsonPath("$.data.status").value("OPEN"));

            verify(groupPurchaseService, times(1)).getGroupPurchaseById(purchaseId);
        }

        @Test
        @DisplayName("잘못된 purchaseId UUID 형식이면 400 에러가 발생합니다.")
        void getGroupPurchaseById_invalidUUID() throws Exception {
            // when & then
            mockMvc.perform(
                            get("/api/purchases/{purchaseId}", "uuid")
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.message").value("적절하지 않은 요청 값이 존재합니다."));

            verify(groupPurchaseService, never()).getGroupPurchaseById(any());
        }
    }

    @Nested
    @DisplayName("공동구매 목록 조회 API")
    class GetGroupPurchaseListTest {

        @Test
        @DisplayName("공동구매 목록을 페이징하여 조회합니다.")
        void getGroupPurchaseList_success() throws Exception {
            // given
            OffsetDateTime now = OffsetDateTime.now();

            GroupPurchaseThumbnailInfo info1 = new GroupPurchaseThumbnailInfo(
                    UUID.randomUUID(),
                    10,
                    100,
                    "테스트 공동구매 1",
                    15000L,
                    50,
                    now.minusDays(1),
                    now.plusDays(7),
                    ProductCategory.FOOD,
                    GroupPurchaseStatus.OPEN,
                    now.minusDays(2)
            );

            GroupPurchaseThumbnailInfo info2 = new GroupPurchaseThumbnailInfo(
                    UUID.randomUUID(),
                    5,
                    50,
                    "테스트 공동구매 2",
                    25000L,
                    30,
                    now.minusDays(2),
                    now.plusDays(5),
                    ProductCategory.ELECTRONICS,
                    GroupPurchaseStatus.SCHEDULED,
                    now.minusDays(3)
            );

            PageResponse<GroupPurchaseThumbnailInfo> pageResponse = new PageResponse<>(
                    List.of(info1, info2),
                    1,
                    2L,
                    true,
                    true,
                    10,
                    2
            );

            when(groupPurchaseService.getGroupPurchase(any()))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(
                            get("/api/purchases")
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공동구매 목록 조회되었습니다."))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.numberOfElements").value(2))

                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))

                    .andExpect(jsonPath("$.data.content[0].title").value("테스트 공동구매 1"))
                    .andExpect(jsonPath("$.data.content[0].minQuantity").value(10))
                    .andExpect(jsonPath("$.data.content[0].maxQuantity").value(100))
                    .andExpect(jsonPath("$.data.content[0].discountedPrice").value(15000))
                    .andExpect(jsonPath("$.data.content[0].currentQuantity").value(50))
                    .andExpect(jsonPath("$.data.content[0].category").value("FOOD"))
                    .andExpect(jsonPath("$.data.content[0].status").value("OPEN"))

                    .andExpect(jsonPath("$.data.content[1].title").value("테스트 공동구매 2"))
                    .andExpect(jsonPath("$.data.content[1].minQuantity").value(5))
                    .andExpect(jsonPath("$.data.content[1].maxQuantity").value(50))
                    .andExpect(jsonPath("$.data.content[1].discountedPrice").value(25000))
                    .andExpect(jsonPath("$.data.content[1].currentQuantity").value(30))
                    .andExpect(jsonPath("$.data.content[1].category").value("ELECTRONICS"))
                    .andExpect(jsonPath("$.data.content[1].status").value("SCHEDULED"));

            verify(groupPurchaseService, times(1)).getGroupPurchase(any());
        }

        @Test
        @DisplayName("빈 목록도 정상적으로 조회됩니다.")
        void getGroupPurchaseList_empty() throws Exception {
            // given
            PageResponse<GroupPurchaseThumbnailInfo> pageResponse = new PageResponse<>(
                    List.of(),
                    0,
                    0L,
                    true,
                    true,
                    10,
                    0
            );

            when(groupPurchaseService.getGroupPurchase(any()))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(
                            get("/api/purchases")
                                    .param("page", "0")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공동구매 목록 조회되었습니다."))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.size").value(10));

            verify(groupPurchaseService, times(1)).getGroupPurchase(any());
        }

        @Test
        @DisplayName("두 번째 페이지를 조회합니다.")
        void getGroupPurchaseList_secondPage() throws Exception {
            // given
            OffsetDateTime now = OffsetDateTime.now();

            GroupPurchaseThumbnailInfo info = new GroupPurchaseThumbnailInfo(
                    UUID.randomUUID(),
                    3,
                    30,
                    "테스트 공동구매 3",
                    18000L,
                    20,
                    now.minusDays(3),
                    now.plusDays(4),
                    ProductCategory.FASHION,
                    GroupPurchaseStatus.OPEN,
                    now.minusDays(4)
            );

            PageResponse<GroupPurchaseThumbnailInfo> pageResponse = new PageResponse<>(
                    List.of(info),
                    3,
                    25L,
                    false,
                    true,
                    10,
                    1
            );

            when(groupPurchaseService.getGroupPurchase(any()))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(
                            get("/api/purchases")
                                    .param("page", "2")
                                    .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("공동구매 목록 조회되었습니다."))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(25))
                    .andExpect(jsonPath("$.data.totalPages").value(3))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.numberOfElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("테스트 공동구매 3"))
                    .andExpect(jsonPath("$.data.content[0].minQuantity").value(3))
                    .andExpect(jsonPath("$.data.content[0].maxQuantity").value(30))
                    .andExpect(jsonPath("$.data.content[0].discountedPrice").value(18000))
                    .andExpect(jsonPath("$.data.content[0].currentQuantity").value(20))
                    .andExpect(jsonPath("$.data.content[0].category").value("FASHION"))
                    .andExpect(jsonPath("$.data.content[0].status").value("OPEN"));

            verify(groupPurchaseService, times(1)).getGroupPurchase(any());
        }
    }
}
