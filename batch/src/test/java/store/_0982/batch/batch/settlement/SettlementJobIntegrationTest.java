package store._0982.batch.batch.settlement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import store._0982.batch.BatchApplicationTests;
import store._0982.batch.config.BatchTestConfig;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(BatchTestConfig.class)
@DisplayName("Settlement Job 통합 테스트")
class SettlementJobIntegrationTest extends BatchApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job settlementJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(settlementJob);
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_balance_history");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_balance");
        jdbcTemplate.execute("DELETE FROM settlement_schema.order_settlement");
    }

    private UUID insertOrderSettlement(UUID sellerId, long settlementAmount, OffsetDateTime settledAt) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.order_settlement
                (order_settlement_id, seller_id, group_purchase_id, order_id,
                 order_settlement_status, order_amount, platform_fee_rate, platform_fee,
                 settlement_amount, created_at, settled_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                """,
                id,
                sellerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "COMPLETED",
                (long) (settlementAmount / 0.95),
                0.05,
                (long) ((settlementAmount / 0.95) * 0.05),
                settlementAmount,
                settledAt
        );
        return id;
    }

    private UUID insertUnsettledOrderSettlement(UUID sellerId, long settlementAmount) {
        return insertOrderSettlement(sellerId, settlementAmount, null);
    }

    private UUID insertNegativeAmountOrderSettlement(UUID sellerId, long negativeAmount) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.order_settlement
                (order_settlement_id, seller_id, group_purchase_id, order_id,
                 order_settlement_status, order_amount, platform_fee_rate, platform_fee,
                 settlement_amount, created_at, settled_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, NULL)
                """,
                id,
                sellerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "COMPLETED",
                0L,
                0.0,
                0L,
                negativeAmount
        );
        return id;
    }

    private UUID insertSellerBalance(UUID sellerId, long balance) {
        UUID balanceId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.seller_balance
                (balance_id, member_id, settlement_balance, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                balanceId, sellerId, balance
        );
        return balanceId;
    }

    private Long getSellerBalance(UUID sellerId) {
        return jdbcTemplate.queryForObject(
                "SELECT settlement_balance FROM settlement_schema.seller_balance WHERE member_id = ?",
                Long.class,
                sellerId
        );
    }

    private OffsetDateTime getSettledAt(UUID orderSettlementId) {
        return jdbcTemplate.queryForObject(
                "SELECT settled_at FROM settlement_schema.order_settlement WHERE order_settlement_id = ?",
                OffsetDateTime.class,
                orderSettlementId
        );
    }

    private int countBalanceHistory(UUID sellerId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlement_schema.seller_balance_history WHERE member_id = ?",
                Integer.class,
                sellerId
        );
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

    @Nested
    @DisplayName("settlementStep 테스트")
    class SettlementStepTest {

        @Test
        @DisplayName("미정산 항목이 있으면 판매자 잔액이 증가하고 settled_at이 갱신된다")
        void step_shouldIncreaseBalanceAndMarkSettled() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            UUID settlementId = insertUnsettledOrderSettlement(sellerId, 10_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerBalance(sellerId)).isEqualTo(10_000L);
            assertThat(getSettledAt(settlementId)).isNotNull();
        }

        @Test
        @DisplayName("이미 정산된(settled_at IS NOT NULL) 항목은 다시 처리하지 않는다")
        void step_shouldSkipAlreadySettledItems() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 5_000L);
            insertOrderSettlement(sellerId, 10_000L, OffsetDateTime.now().minusDays(1));

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            // 기존 잔액 그대로여야 함
            assertThat(getSellerBalance(sellerId)).isEqualTo(5_000L);
        }

        @Test
        @DisplayName("여러 판매자의 미정산 항목을 각각 독립적으로 처리한다")
        void step_shouldProcessMultipleSellersIndependently() {
            // given
            UUID sellerId1 = UUID.randomUUID();
            UUID sellerId2 = UUID.randomUUID();
            insertSellerBalance(sellerId1, 0L);
            insertSellerBalance(sellerId2, 1_000L);
            insertUnsettledOrderSettlement(sellerId1, 20_000L);
            insertUnsettledOrderSettlement(sellerId2, 30_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerBalance(sellerId1)).isEqualTo(20_000L);
            assertThat(getSellerBalance(sellerId2)).isEqualTo(31_000L);
        }

        @Test
        @DisplayName("동일 판매자의 여러 미정산 항목이 합산된다")
        void step_shouldAccumulateAmountsForSameSeller() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            insertUnsettledOrderSettlement(sellerId, 10_000L);
            insertUnsettledOrderSettlement(sellerId, 20_000L);
            insertUnsettledOrderSettlement(sellerId, 5_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerBalance(sellerId)).isEqualTo(35_000L);
        }

        @Test
        @DisplayName("판매자 잔액이 없으면 새로 생성하여 정산액을 반영한다")
        void step_shouldCreateSellerBalanceWhenNotExists() {
            // given: SellerBalance row 없음
            UUID sellerId = UUID.randomUUID();
            insertUnsettledOrderSettlement(sellerId, 15_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerBalance(sellerId)).isEqualTo(15_000L);
        }

        @Test
        @DisplayName("정산 처리 후 SellerBalanceHistory(CREDIT)가 건별로 생성된다")
        void step_shouldCreateCreditHistoryPerSettlement() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            insertUnsettledOrderSettlement(sellerId, 10_000L);
            insertUnsettledOrderSettlement(sellerId, 20_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countBalanceHistory(sellerId)).isEqualTo(2);
        }

        @Test
        @DisplayName("settlement_amount가 음수인 항목이 있으면 Step이 실패한다")
        void step_shouldFailWhenSettlementAmountIsNegative() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            insertNegativeAmountOrderSettlement(sellerId, -5_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        }

        @Test
        @DisplayName("처리할 미정산 항목이 없으면 Step이 정상 완료된다")
        void step_shouldCompleteWithNoUnsettledItems() {
            // given: 미정산 항목 없음

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "settlementStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(execution.getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
        }
    }

    @Nested
    @DisplayName("전체 Job 테스트")
    class FullJobTest {

        @Test
        @DisplayName("전체 Job이 정상적으로 완료된다")
        void job_shouldCompleteSuccessfully() throws Exception {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            insertUnsettledOrderSettlement(sellerId, 10_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        }

        @Test
        @DisplayName("처리할 데이터가 없어도 Job이 정상 완료된다")
        void job_shouldCompleteWithNoData() throws Exception {
            // given: 미정산 항목 없음

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        }

        @Test
        @DisplayName("다수의 판매자와 다수의 정산 항목을 한 번에 처리한다")
        void job_shouldProcessLargeDataSet() throws Exception {
            // given: 판매자 3명, 각 2건씩 총 6건
            List<UUID> sellerIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            for (UUID sellerId : sellerIds) {
                insertSellerBalance(sellerId, 0L);
                insertUnsettledOrderSettlement(sellerId, 10_000L);
                insertUnsettledOrderSettlement(sellerId, 10_000L);
            }

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            for (UUID sellerId : sellerIds) {
                assertThat(getSellerBalance(sellerId)).isEqualTo(20_000L);
                assertThat(countBalanceHistory(sellerId)).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("미정산 항목과 기정산 항목이 섞여 있을 때 미정산 항목만 처리된다")
        void job_shouldOnlyProcessUnsettledItems() throws Exception {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 100_000L);
            insertUnsettledOrderSettlement(sellerId, 50_000L);
            insertOrderSettlement(sellerId, 30_000L, OffsetDateTime.now().minusDays(1));

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerBalance(sellerId)).isEqualTo(150_000L);
            assertThat(countBalanceHistory(sellerId)).isEqualTo(1);
        }

        @Test
        @DisplayName("Step 실패 시 Job도 FAILED 상태가 된다")
        void job_shouldFailWhenStepFails() throws Exception {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            insertNegativeAmountOrderSettlement(sellerId, -1_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
            assertThat(execution.getStepExecutions()).anyMatch(
                    step -> step.getStatus().isUnsuccessful()
            );
        }
    }
}
