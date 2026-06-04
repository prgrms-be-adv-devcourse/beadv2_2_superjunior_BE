package store._0982.batch.batch.grouppurchase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import store._0982.batch.BatchApplicationTests;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GroupPurchase Job 통합 테스트")
class GroupPurchaseJobIntegrationTest extends BatchApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job groupPurchaseJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(groupPurchaseJob);
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("DELETE FROM product_schema.group_purchase");
        jdbcTemplate.execute("DELETE FROM product_schema.product");
    }

    @Nested
    @DisplayName("openGroupPurchaseStep 테스트")
    class OpenGroupPurchaseStepTest {

        @Test
        @DisplayName("시작 시간이 지난 SCHEDULED 공동구매를 OPEN으로 변경한다")
        void openStep_shouldChangeScheduledToOpen() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.SCHEDULED,
                    OffsetDateTime.now().minusHours(1),
                    OffsetDateTime.now().plusDays(7),
                    10,
                    0
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep("openGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.OPEN.name());
        }

        @Test
        @DisplayName("시작 시간이 지나지 않은 SCHEDULED 공동구매는 변경하지 않는다")
        void openStep_shouldNotChangeIfStartDateNotReached() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.SCHEDULED,
                    OffsetDateTime.now().plusHours(1),
                    OffsetDateTime.now().plusDays(7),
                    10,
                    0
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep("openGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.SCHEDULED.name());
        }

        @Test
        @DisplayName("OPEN 상태의 공동구매는 처리하지 않는다")
        void openStep_shouldNotProcessOpenStatus() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(1),
                    OffsetDateTime.now().plusDays(7),
                    10,
                    5
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep("openGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.OPEN.name());
        }
    }

    @Nested
    @DisplayName("updateStatusClosedGroupPurchaseStep 테스트")
    class UpdateStatusClosedGroupPurchaseStepTest {

        @Test
        @DisplayName("종료 시간이 지나고 최소 수량을 달성한 공동구매는 SUCCESS로 변경한다")
        void closeStep_shouldChangeToSuccessWhenMinQuantityReached() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(7),
                    OffsetDateTime.now().minusHours(1),
                    10,
                    15
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                    "updateStatusClosedGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.SUCCESS.name());
        }

        @Test
        @DisplayName("종료 시간이 지나고 최소 수량을 달성하지 못한 공동구매는 FAILED로 변경한다")
        void closeStep_shouldChangeToFailedWhenMinQuantityNotReached() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(7),
                    OffsetDateTime.now().minusHours(1),
                    10,
                    5
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                    "updateStatusClosedGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.FAILED.name());
        }

        @Test
        @DisplayName("종료 시간이 지나지 않은 OPEN 공동구매는 변경하지 않는다")
        void closeStep_shouldNotChangeIfEndDateNotReached() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(1),
                    OffsetDateTime.now().plusDays(7),
                    10,
                    15
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                    "updateStatusClosedGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.OPEN.name());
        }

        @Test
        @DisplayName("정확히 최소 수량과 같을 때 SUCCESS로 변경한다")
        void closeStep_shouldSuccessWhenExactlyMinQuantity() throws Exception {
            // given
            UUID productId = insertProduct();
            UUID groupPurchaseId = insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(7),
                    OffsetDateTime.now().minusHours(1),
                    10,
                    10
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                    "updateStatusClosedGroupPurchaseStep",
                    createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = getGroupPurchaseStatus(groupPurchaseId);
            assertThat(status).isEqualTo(GroupPurchaseStatus.SUCCESS.name());
        }
    }

    @Nested
    @DisplayName("전체 Job 테스트")
    class FullJobTest {

        @Test
        @DisplayName("전체 Job이 정상적으로 실행된다")
        void job_shouldCompleteSuccessfully() throws Exception {
            // given
            UUID productId = insertProduct();
            insertGroupPurchase(
                    productId,
                    GroupPurchaseStatus.SCHEDULED,
                    OffsetDateTime.now().minusHours(1),
                    OffsetDateTime.now().plusDays(7),
                    10,
                    0
            );

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        }

        @Test
        @DisplayName("여러 공동구매를 동시에 처리한다")
        void job_shouldProcessMultipleGroupPurchases() throws Exception {
            // given
            UUID productId1 = insertProduct();
            UUID productId2 = insertProduct();
            UUID productId3 = insertProduct();

            UUID gp1 = insertGroupPurchase(productId1, GroupPurchaseStatus.SCHEDULED,
                    OffsetDateTime.now().minusHours(1), OffsetDateTime.now().plusDays(7), 10, 0);
            UUID gp2 = insertGroupPurchase(productId2, GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(7), OffsetDateTime.now().minusHours(1), 10, 15);
            UUID gp3 = insertGroupPurchase(productId3, GroupPurchaseStatus.OPEN,
                    OffsetDateTime.now().minusDays(7), OffsetDateTime.now().minusHours(1), 10, 5);

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getGroupPurchaseStatus(gp1)).isEqualTo(GroupPurchaseStatus.OPEN.name());
            assertThat(getGroupPurchaseStatus(gp2)).isEqualTo(GroupPurchaseStatus.SUCCESS.name());
            assertThat(getGroupPurchaseStatus(gp3)).isEqualTo(GroupPurchaseStatus.FAILED.name());
        }

        @Test
        @DisplayName("처리할 데이터가 없어도 Job은 정상 완료된다")
        void job_shouldCompleteWithNoData() throws Exception {
            // given - no data

            // when
            JobExecution jobExecution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        }
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("now", OffsetDateTime.now().toString())
                .toJobParameters();
    }

    private UUID insertProduct() {
        UUID productId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO product_schema.product
            (product_id, seller_id, title, description, price, stock, category)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
                productId,
                UUID.randomUUID(),
                "테스트 상품",
                "테스트 설명",
                15000L,
                100,
                "FOOD"
        );
        return productId;
    }

    private UUID insertGroupPurchase(UUID productId,
                                      GroupPurchaseStatus status,
                                      OffsetDateTime startDate,
                                      OffsetDateTime endDate,
                                      int minQuantity,
                                      int currentQuantity) {
        UUID groupPurchaseId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO product_schema.group_purchase
            (group_purchase_id, product_id, seller_id, title, description, discounted_price,
             status, start_date, end_date, min_quantity, max_quantity, current_quantity, version)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                groupPurchaseId,
                productId,
                UUID.randomUUID(),
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                status.name(),
                startDate,
                endDate,
                minQuantity,
                100,
                currentQuantity,
                0L
        );
        return groupPurchaseId;
    }

    private String getGroupPurchaseStatus(UUID groupPurchaseId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM product_schema.group_purchase WHERE group_purchase_id = ?",
                String.class,
                groupPurchaseId
        );
    }
}
