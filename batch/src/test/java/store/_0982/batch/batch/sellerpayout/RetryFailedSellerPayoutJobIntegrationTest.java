package store._0982.batch.batch.sellerpayout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import store._0982.batch.BatchApplicationTests;
import store._0982.batch.batch.sellerpayout.policy.SellerPayoutPolicy;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RetryFailedSellerPayout Job 통합 테스트")
class RetryFailedSellerPayoutJobIntegrationTest extends BatchApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job retryFailedSellerPayoutJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(retryFailedSellerPayoutJob);
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_payout_failure");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_payout");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_balance_history");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_balance");
        jdbcTemplate.execute("DELETE FROM member_schema.seller");
    }

    private UUID insertSellerPayout(UUID sellerId, long totalAmount, String status) {
        UUID payoutId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.seller_payout
                (seller_payout_id, seller_id, period_start, period_end,
                 total_amount, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                payoutId, sellerId,
                OffsetDateTime.now().minusMonths(1),
                OffsetDateTime.now(),
                totalAmount, status
        );
        return payoutId;
    }

    private UUID insertSellerPayoutFailure(UUID sellerPayoutId, UUID sellerId, int retryCount) {
        UUID failureId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.seller_payout_failure
                (failure_id, seller_payout_id, seller_id, period_start, period_end,
                 failure_reason, retry_count, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                failureId, sellerPayoutId, sellerId,
                OffsetDateTime.now().minusMonths(1),
                OffsetDateTime.now(),
                "INVALID_ACCOUNT_INFO",
                retryCount
        );
        return failureId;
    }

    private void insertSellerBalance(UUID sellerId, long balance) {
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.seller_balance
                (balance_id, member_id, settlement_balance, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), sellerId, balance);
    }

    private void insertSellerAccount(UUID sellerId, String bankCode, String accountNumber) {
        jdbcTemplate.update("""
                INSERT INTO member_schema.seller
                (seller_id, bank_code, account_number, account_holder,
                 business_registration_number, created_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, sellerId, bankCode, accountNumber, "테스트판매자", "123-45-67890");
    }

    private String getSellerPayoutStatus(UUID sellerPayoutId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM settlement_schema.seller_payout WHERE seller_payout_id = ?",
                String.class, sellerPayoutId);
    }

    private int countSellerPayoutFailure(UUID sellerPayoutId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlement_schema.seller_payout_failure WHERE seller_payout_id = ?",
                Integer.class, sellerPayoutId);
    }

    private int getRetryCount(UUID sellerPayoutId) {
        return jdbcTemplate.queryForObject(
                "SELECT retry_count FROM settlement_schema.seller_payout_failure WHERE seller_payout_id = ?",
                Integer.class, sellerPayoutId);
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

    @Nested
    @DisplayName("retryFailedSellerPayoutStep 테스트")
    class RetryStepTest {

        @Test
        @DisplayName("계좌 정보가 복구된 실패 건은 재시도 시 COMPLETED 처리된다")
        void step_shouldCompleteOnRetryWhenAccountRestored() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);
            insertSellerAccount(sellerId, "004", "123456789");

            UUID payoutId = insertSellerPayout(sellerId, 50_000L, "FAILED");
            insertSellerPayoutFailure(payoutId, sellerId, 1);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "retryFailedSellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerPayoutStatus(payoutId)).isEqualTo("COMPLETED");
            assertThat(countSellerPayoutFailure(payoutId)).isEqualTo(0);
        }

        @Test
        @DisplayName("재시도 성공 후 SellerPayoutFailure 레코드가 삭제된다")
        void step_shouldDeleteFailureRecordOnSuccess() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);
            insertSellerAccount(sellerId, "004", "123456789");

            UUID payoutId = insertSellerPayout(sellerId, 50_000L, "FAILED");
            insertSellerPayoutFailure(payoutId, sellerId, 2);

            // when
            jobLauncherTestUtils.launchStep("retryFailedSellerPayoutStep", createJobParameters());

            // then
            assertThat(countSellerPayoutFailure(payoutId)).isEqualTo(0);
        }

        @Test
        @DisplayName("계좌 정보가 여전히 없으면 재시도 실패 후 retryCount가 증가한다")
        void step_shouldIncrementRetryCountOnFailure() {
            // given: 계좌 정보 없음
            UUID sellerId = UUID.randomUUID();
            UUID payoutId = insertSellerPayout(sellerId, 50_000L, "FAILED");
            insertSellerPayoutFailure(payoutId, sellerId, 1);

            // when
            jobLauncherTestUtils.launchStep("retryFailedSellerPayoutStep", createJobParameters());

            // then
            assertThat(getRetryCount(payoutId)).isEqualTo(2);
            assertThat(getSellerPayoutStatus(payoutId)).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("retryCount가 MAX_RETRY 이상인 실패 건은 처리하지 않는다")
        void step_shouldSkipFailureExceedingMaxRetry() {
            // given: retryCount = MAX_RETRY(5)
            UUID sellerId = UUID.randomUUID();
            insertSellerAccount(sellerId, "004", "123456789");
            UUID payoutId = insertSellerPayout(sellerId, 50_000L, "FAILED");
            insertSellerPayoutFailure(payoutId, sellerId, SellerPayoutPolicy.MAX_RETRY);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "retryFailedSellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            // 처리되지 않았으므로 retryCount 그대로
            assertThat(getRetryCount(payoutId)).isEqualTo(SellerPayoutPolicy.MAX_RETRY);
            assertThat(getSellerPayoutStatus(payoutId)).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("이미 COMPLETED인 SellerPayout은 재처리하지 않는다")
        void step_shouldSkipCompletedPayout() {
            // given: 이미 완료된 payout에 대한 실패 기록 (비정상 데이터)
            UUID sellerId = UUID.randomUUID();
            UUID payoutId = insertSellerPayout(sellerId, 50_000L, "COMPLETED");
            insertSellerPayoutFailure(payoutId, sellerId, 0);

            // when
            jobLauncherTestUtils.launchStep("retryFailedSellerPayoutStep", createJobParameters());

            // then: processor에서 null 반환으로 필터링되어 status 변화 없음
            assertThat(getSellerPayoutStatus(payoutId)).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("처리할 실패 건이 없으면 Step이 정상 완료된다")
        void step_shouldCompleteWithNoFailures() {
            // given: 실패 기록 없음

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "retryFailedSellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("전체 Job 테스트")
    class FullJobTest {

        @Test
        @DisplayName("전체 Job이 정상 완료된다")
        void job_shouldCompleteSuccessfully() throws Exception {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);
            insertSellerAccount(sellerId, "004", "123456789");

            UUID payoutId = insertSellerPayout(sellerId, 50_000L, "FAILED");
            insertSellerPayoutFailure(payoutId, sellerId, 1);

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(getSellerPayoutStatus(payoutId)).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("처리할 데이터가 없어도 Job이 정상 완료된다")
        void job_shouldCompleteWithNoData() throws Exception {
            // given: 데이터 없음

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        }
    }
}
