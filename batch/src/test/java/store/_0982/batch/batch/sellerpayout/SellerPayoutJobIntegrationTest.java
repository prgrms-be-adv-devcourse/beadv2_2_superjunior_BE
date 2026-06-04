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

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerPayout Job 통합 테스트")
class SellerPayoutJobIntegrationTest extends BatchApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job sellerPayoutJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(sellerPayoutJob);
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_payout_failure");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_payout");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_balance_history");
        jdbcTemplate.execute("DELETE FROM settlement_schema.seller_balance");
        jdbcTemplate.execute("DELETE FROM member_schema.seller");
    }

    private UUID insertSellerBalance(UUID sellerId, long balance) {
        UUID balanceId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO settlement_schema.seller_balance
                (balance_id, member_id, settlement_balance, created_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """, balanceId, sellerId, balance);
        return balanceId;
    }

    private void insertSellerAccount(UUID sellerId, String bankCode, String accountNumber) {
        jdbcTemplate.update("""
                INSERT INTO member_schema.seller
                (seller_id, bank_code, account_number, account_holder,
                 business_registration_number, created_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, sellerId, bankCode, accountNumber, "테스트판매자", "123-45-67890");
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

    private String getSellerPayoutStatus(UUID sellerPayoutId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM settlement_schema.seller_payout WHERE seller_payout_id = ?",
                String.class, sellerPayoutId);
    }

    private Long getSellerBalance(UUID sellerId) {
        return jdbcTemplate.queryForObject(
                "SELECT settlement_balance FROM settlement_schema.seller_balance WHERE member_id = ?",
                Long.class, sellerId);
    }

    private int countSellerPayout(UUID sellerId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlement_schema.seller_payout WHERE seller_id = ?",
                Integer.class, sellerId);
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

    private int countBalanceHistory(UUID sellerId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM settlement_schema.seller_balance_history WHERE member_id = ?",
                Integer.class, sellerId);
    }

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }

    @Nested
    @DisplayName("sellerPayoutStep 테스트")
    class SellerPayoutStepTest {

        @Test
        @DisplayName("잔액이 최소 송금액 이상인 판매자는 COMPLETED 처리된다")
        void step_shouldCompletePayoutForEligibleSeller() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);
            insertSellerAccount(sellerId, "004", "123456789");

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "sellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countSellerPayout(sellerId)).isEqualTo(1);
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM settlement_schema.seller_payout WHERE seller_id = ?",
                    String.class, sellerId);
            assertThat(status).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("COMPLETED 처리 후 판매자 잔액이 0으로 초기화된다")
        void step_shouldClearBalanceAfterSuccessfulPayout() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);
            insertSellerAccount(sellerId, "004", "123456789");

            // when
            jobLauncherTestUtils.launchStep("sellerPayoutStep", createJobParameters());

            // then
            assertThat(getSellerBalance(sellerId)).isEqualTo(0L);
        }

        @Test
        @DisplayName("COMPLETED 처리 후 SellerBalanceHistory(DEBIT)가 생성된다")
        void step_shouldCreateDebitHistoryAfterSuccessfulPayout() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);
            insertSellerAccount(sellerId, "004", "123456789");

            // when
            jobLauncherTestUtils.launchStep("sellerPayoutStep", createJobParameters());

            // then
            assertThat(countBalanceHistory(sellerId)).isEqualTo(1);
        }

        @Test
        @DisplayName("계좌 정보가 없는 판매자는 FAILED 처리되고 SellerPayoutFailure가 생성된다")
        void step_shouldFailPayoutWhenAccountInfoMissing() {
            // given: 계좌 정보 없음
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "sellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM settlement_schema.seller_payout WHERE seller_id = ?",
                    String.class, sellerId);
            assertThat(status).isEqualTo("FAILED");

            UUID payoutId = jdbcTemplate.queryForObject(
                    "SELECT seller_payout_id FROM settlement_schema.seller_payout WHERE seller_id = ?",
                    UUID.class, sellerId);
            assertThat(countSellerPayoutFailure(payoutId)).isEqualTo(1);
        }

        @Test
        @DisplayName("잔액이 최소 송금액 미만인 판매자는 sellerPayoutStep에서 처리하지 않는다")
        void step_shouldSkipSellerBelowMinimumAmount() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 10_000L); // MINIMUM_TRANSFER_AMOUNT(30000) 미만
            insertSellerAccount(sellerId, "004", "123456789");

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "sellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countSellerPayout(sellerId)).isEqualTo(0);
        }

        @Test
        @DisplayName("잔액이 0인 판매자는 처리하지 않는다")
        void step_shouldSkipSellerWithZeroBalance() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);
            insertSellerAccount(sellerId, "004", "123456789");

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "sellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countSellerPayout(sellerId)).isEqualTo(0);
        }

        @Test
        @DisplayName("처리 대상이 없으면 Step이 정상 완료된다")
        void step_shouldCompleteWithNoEligibleSellers() {
            // given: 데이터 없음

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "sellerPayoutStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("lowBalanceNotificationStep 테스트")
    class LowBalanceNotificationStepTest {

        @Test
        @DisplayName("잔액이 0 초과 최소 송금액 미만인 판매자는 DEFERRED 처리된다")
        void step_shouldDeferPayoutForLowBalanceSeller() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 10_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "lowBalanceNotificationStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countSellerPayout(sellerId)).isEqualTo(1);
            String status = jdbcTemplate.queryForObject(
                    "SELECT status FROM settlement_schema.seller_payout WHERE seller_id = ?",
                    String.class, sellerId);
            assertThat(status).isEqualTo("DEFERRED");
        }

        @Test
        @DisplayName("잔액이 최소 송금액 이상인 판매자는 lowBalanceNotificationStep에서 처리하지 않는다")
        void step_shouldSkipSellerAboveMinimumAmount() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 50_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "lowBalanceNotificationStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countSellerPayout(sellerId)).isEqualTo(0);
        }

        @Test
        @DisplayName("잔액이 0인 판매자는 lowBalanceNotificationStep에서 처리하지 않는다")
        void step_shouldSkipSellerWithZeroBalance() {
            // given
            UUID sellerId = UUID.randomUUID();
            insertSellerBalance(sellerId, 0L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchStep(
                    "lowBalanceNotificationStep", createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(countSellerPayout(sellerId)).isEqualTo(0);
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

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        }

        @Test
        @DisplayName("잔액 구간에 따라 sellerPayoutStep과 lowBalanceNotificationStep이 각각 처리된다")
        void job_shouldRouteSellersByBalance() throws Exception {
            // given
            UUID highSeller = UUID.randomUUID();   // 송금 대상
            UUID lowSeller = UUID.randomUUID();    // 알림 대상

            insertSellerBalance(highSeller, 50_000L);
            insertSellerAccount(highSeller, "004", "111111111");

            insertSellerBalance(lowSeller, 10_000L);

            // when
            JobExecution execution = jobLauncherTestUtils.launchJob(createJobParameters());

            // then
            assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

            String highStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM settlement_schema.seller_payout WHERE seller_id = ?",
                    String.class, highSeller);
            assertThat(highStatus).isEqualTo("COMPLETED");

            String lowStatus = jdbcTemplate.queryForObject(
                    "SELECT status FROM settlement_schema.seller_payout WHERE seller_id = ?",
                    String.class, lowSeller);
            assertThat(lowStatus).isEqualTo("DEFERRED");
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
