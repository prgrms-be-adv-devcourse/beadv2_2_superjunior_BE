package store._0982.batch.batch.sellerpayout.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.batch.batch.sellerpayout.dto.SellerBalanceDto;
import store._0982.batch.batch.sellerpayout.policy.SellerPayoutPolicy;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutStatus;

import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerPayoutProcessor 단위 테스트")
class SellerPayoutProcessorTest {

    private SellerPayoutProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new SellerPayoutProcessor();
    }

    @Test
    @DisplayName("SellerBalanceDto로 PENDING 상태의 SellerPayout을 생성한다")
    void process_shouldCreatePendingSellerPayout() throws Exception {
        // given
        UUID sellerId = UUID.randomUUID();
        SellerBalanceDto dto = new SellerBalanceDto(sellerId, 50_000L);

        // when
        SellerPayout result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(sellerId);
        assertThat(result.getTotalAmount()).isEqualTo(50_000L);
        assertThat(result.getStatus()).isEqualTo(SellerPayoutStatus.PENDING);
    }

    @Test
    @DisplayName("period는 지난 달의 시작일과 마지막일로 설정된다")
    void process_shouldSetPeriodToLastMonth() throws Exception {
        // given
        SellerBalanceDto dto = new SellerBalanceDto(UUID.randomUUID(), 50_000L);
        YearMonth lastMonth = YearMonth.now(SellerPayoutPolicy.KOREA_ZONE).minusMonths(1);

        // when
        SellerPayout result = processor.process(dto);

        // then
        assertThat(result.getPeriodStart().getYear()).isEqualTo(lastMonth.getYear());
        assertThat(result.getPeriodStart().getMonthValue()).isEqualTo(lastMonth.getMonthValue());
        assertThat(result.getPeriodStart().getDayOfMonth()).isEqualTo(1);

        assertThat(result.getPeriodEnd().getYear()).isEqualTo(lastMonth.getYear());
        assertThat(result.getPeriodEnd().getMonthValue()).isEqualTo(lastMonth.getMonthValue());
        assertThat(result.getPeriodEnd().getDayOfMonth()).isEqualTo(lastMonth.lengthOfMonth());
    }

    @Test
    @DisplayName("계좌 정보는 null로 생성된다 (Writer에서 설정)")
    void process_shouldCreatePayoutWithNullAccountInfo() throws Exception {
        // given
        SellerBalanceDto dto = new SellerBalanceDto(UUID.randomUUID(), 50_000L);

        // when
        SellerPayout result = processor.process(dto);

        // then
        assertThat(result.getAccountNumber()).isNull();
        assertThat(result.getBankCode()).isNull();
    }

    @Test
    @DisplayName("sellerPayoutId는 자동 생성된다")
    void process_shouldAutoGenerateSellerPayoutId() throws Exception {
        // given
        SellerBalanceDto dto = new SellerBalanceDto(UUID.randomUUID(), 50_000L);

        // when
        SellerPayout result = processor.process(dto);

        // then
        assertThat(result.getSellerPayoutId()).isNotNull();
    }

    @Test
    @DisplayName("서로 다른 호출마다 독립적인 SellerPayout이 생성된다")
    void process_shouldCreateIndependentPayouts() throws Exception {
        // given
        UUID sellerId1 = UUID.randomUUID();
        UUID sellerId2 = UUID.randomUUID();

        // when
        SellerPayout result1 = processor.process(new SellerBalanceDto(sellerId1, 30_000L));
        SellerPayout result2 = processor.process(new SellerBalanceDto(sellerId2, 70_000L));

        // then
        assertThat(result1.getSellerPayoutId()).isNotEqualTo(result2.getSellerPayoutId());
        assertThat(result1.getSellerId()).isEqualTo(sellerId1);
        assertThat(result2.getSellerId()).isEqualTo(sellerId2);
        assertThat(result1.getTotalAmount()).isEqualTo(30_000L);
        assertThat(result2.getTotalAmount()).isEqualTo(70_000L);
    }
}
