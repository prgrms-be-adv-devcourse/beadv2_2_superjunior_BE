package store._0982.batch.batch.grouppurchase.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenGroupPurchaseProcessor 단위 테스트")
class OpenGroupPurchaseProcessorTest {

    private OpenGroupPurchaseProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OpenGroupPurchaseProcessor();
    }

    @Test
    @DisplayName("SCHEDULED 상태의 공동구매를 OPEN 상태로 변경한다")
    void process_shouldChangeStatusToOpen() throws Exception {
        // given
        GroupPurchaseProjection projection = createProjection(
                GroupPurchaseStatus.SCHEDULED,
                0,
                10
        );

        // when
        GroupPurchaseResultProjection result = processor.process(projection);

        // then
        assertThat(result).isNotNull();
        assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.OPEN);
        assertThat(result.success()).isTrue();
        assertThat(result.groupPurchaseId()).isEqualTo(projection.groupPurchaseId());
    }

    @Test
    @DisplayName("Processor는 원본 데이터를 그대로 유지한다")
    void process_shouldPreserveOriginalData() throws Exception {
        // given
        UUID groupPurchaseId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String title = "테스트 공동구매";
        String description = "테스트 설명";
        Long discountedPrice = 10000L;
        Long originalPrice = 15000L;
        int currentQuantity = 5;
        int minQuantity = 10;

        GroupPurchaseProjection projection = new GroupPurchaseProjection(
                groupPurchaseId,
                GroupPurchaseStatus.SCHEDULED,
                currentQuantity,
                minQuantity,
                sellerId,
                productId,
                title,
                description,
                discountedPrice,
                OffsetDateTime.now().plusDays(7),
                OffsetDateTime.now(),
                originalPrice,
                ProductCategory.FOOD
        );

        // when
        GroupPurchaseResultProjection result = processor.process(projection);

        // then
        assertThat(result.groupPurchaseId()).isEqualTo(groupPurchaseId);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.description()).isEqualTo(description);
        assertThat(result.discountedPrice()).isEqualTo(discountedPrice);
        assertThat(result.originalPrice()).isEqualTo(originalPrice);
        assertThat(result.currentQuantity()).isEqualTo(currentQuantity);
        assertThat(result.minQuantity()).isEqualTo(minQuantity);
        assertThat(result.productCategory()).isEqualTo(ProductCategory.FOOD);
    }

    private GroupPurchaseProjection createProjection(GroupPurchaseStatus status,
                                                      int currentQuantity,
                                                      int minQuantity) {
        return new GroupPurchaseProjection(
                UUID.randomUUID(),
                status,
                currentQuantity,
                minQuantity,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                OffsetDateTime.now().plusDays(7),
                OffsetDateTime.now(),
                15000L,
                ProductCategory.FOOD
        );
    }
}
