package store._0982.batch.batch.grouppurchase.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateStatusClosedGroupPurchaseProcessor 단위 테스트")
class UpdateStatusClosedGroupPurchaseProcessorTest {

    private UpdateStatusClosedGroupPurchaseProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new UpdateStatusClosedGroupPurchaseProcessor();
    }

    @Nested
    @DisplayName("공동구매 성공 케이스")
    class SuccessCase {

        @Test
        @DisplayName("현재 수량이 최소 수량과 같으면 SUCCESS 상태로 변경한다")
        void process_whenCurrentEqualsMin_shouldReturnSuccess() throws Exception {
            // given
            GroupPurchaseProjection projection = createProjection(10, 10);

            // when
            GroupPurchaseResultProjection result = processor.process(projection);

            // then
            assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("현재 수량이 최소 수량보다 크면 SUCCESS 상태로 변경한다")
        void process_whenCurrentGreaterThanMin_shouldReturnSuccess() throws Exception {
            // given
            GroupPurchaseProjection projection = createProjection(15, 10);

            // when
            GroupPurchaseResultProjection result = processor.process(projection);

            // then
            assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
            assertThat(result.success()).isTrue();
        }

        @ParameterizedTest(name = "현재수량={0}, 최소수량={1}일 때 SUCCESS")
        @CsvSource({
                "10, 10",
                "11, 10",
                "100, 50",
                "1, 1"
        })
        @DisplayName("다양한 성공 케이스 검증")
        void process_variousSuccessCases(int currentQuantity, int minQuantity) throws Exception {
            // given
            GroupPurchaseProjection projection = createProjection(currentQuantity, minQuantity);

            // when
            GroupPurchaseResultProjection result = processor.process(projection);

            // then
            assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("공동구매 실패 케이스")
    class FailureCase {

        @Test
        @DisplayName("현재 수량이 최소 수량보다 작으면 FAILED 상태로 변경한다")
        void process_whenCurrentLessThanMin_shouldReturnFailed() throws Exception {
            // given
            GroupPurchaseProjection projection = createProjection(5, 10);

            // when
            GroupPurchaseResultProjection result = processor.process(projection);

            // then
            assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.FAILED);
            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("현재 수량이 0이면 FAILED 상태로 변경한다")
        void process_whenCurrentIsZero_shouldReturnFailed() throws Exception {
            // given
            GroupPurchaseProjection projection = createProjection(0, 10);

            // when
            GroupPurchaseResultProjection result = processor.process(projection);

            // then
            assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.FAILED);
            assertThat(result.success()).isFalse();
        }

        @ParameterizedTest(name = "현재수량={0}, 최소수량={1}일 때 FAILED")
        @CsvSource({
                "0, 10",
                "9, 10",
                "49, 50",
                "0, 1"
        })
        @DisplayName("다양한 실패 케이스 검증")
        void process_variousFailureCases(int currentQuantity, int minQuantity) throws Exception {
            // given
            GroupPurchaseProjection projection = createProjection(currentQuantity, minQuantity);

            // when
            GroupPurchaseResultProjection result = processor.process(projection);

            // then
            assertThat(result.targetStatus()).isEqualTo(GroupPurchaseStatus.FAILED);
            assertThat(result.success()).isFalse();
        }
    }

    @Test
    @DisplayName("Processor는 원본 데이터를 그대로 유지한다")
    void process_shouldPreserveOriginalData() throws Exception {
        // given
        UUID groupPurchaseId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        GroupPurchaseProjection projection = new GroupPurchaseProjection(
                groupPurchaseId,
                GroupPurchaseStatus.OPEN,
                15,
                10,
                sellerId,
                productId,
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now(),
                15000L,
                ProductCategory.BEAUTY
        );

        // when
        GroupPurchaseResultProjection result = processor.process(projection);

        // then
        assertThat(result.groupPurchaseId()).isEqualTo(groupPurchaseId);
        assertThat(result.sellerId()).isEqualTo(sellerId);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.currentQuantity()).isEqualTo(15);
        assertThat(result.minQuantity()).isEqualTo(10);
        assertThat(result.productCategory()).isEqualTo(ProductCategory.BEAUTY);
    }

    private GroupPurchaseProjection createProjection(int currentQuantity, int minQuantity) {
        return new GroupPurchaseProjection(
                UUID.randomUUID(),
                GroupPurchaseStatus.OPEN,
                currentQuantity,
                minQuantity,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now(),
                15000L,
                ProductCategory.FOOD
        );
    }
}
