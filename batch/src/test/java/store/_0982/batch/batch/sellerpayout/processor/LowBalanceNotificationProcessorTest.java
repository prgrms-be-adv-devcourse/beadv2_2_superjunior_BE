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

@DisplayName("LowBalanceNotificationProcessor 단위 테스트")
class LowBalanceNotificationProcessorTest {

    private LowBalanceNotificationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new LowBalanceNotificationProcessor();
    }

    @Test
    @DisplayName("잔액 부족 판매자의 SellerBalanceDto로 PENDING 상태의 SellerPayout을 생성한다")
    void process_shouldCreatePendingSellerPayout() throws Exception {
        // given
        UUID sellerId = UUID.randomUUID();
        SellerBalanceDto dto = new SellerBalanceDto(sellerId, 10_000L);

        // when
        SellerPayout result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSellerId()).isEqualTo(sellerId);
        assertThat(result.getTotalAmount()).isEqualTo(10_000L);
        assertThat(result.getStatus()).isEqualTo(SellerPayoutStatus.PENDING);
    }

    @Test
    @DisplayName("period는 지난 달의 시작일과 마지막일로 설정된다")
    void process_shouldSetPeriodToLastMonth() throws Exception {
        // given
        SellerBalanceDto dto = new SellerBalanceDto(UUID.randomUUID(), 10_000L);
        YearMonth lastMonth = YearMonth.now(SellerPayoutPolicy.KOREA_ZONE).minusMonths(1);

        // when
        SellerPayout result = processor.process(dto);

        // then
        assertThat(result.getPeriodStart().getMonthValue()).isEqualTo(lastMonth.getMonthValue());
        assertThat(result.getPeriodEnd().getDayOfMonth()).isEqualTo(lastMonth.lengthOfMonth());
    }

    @Test
    @DisplayName("SellerPayoutProcessor와 동일한 SellerPayout 구조를 생성한다")
    void process_shouldProduceSameStructureAsSellerPayoutProcessor() throws Exception {
        // given
        UUID sellerId = UUID.randomUUID();
        SellerBalanceDto dto = new SellerBalanceDto(sellerId, 10_000L);

        LowBalanceNotificationProcessor lowBalanceProcessor = new LowBalanceNotificationProcessor();
        SellerPayoutProcessor payoutProcessor = new SellerPayoutProcessor();

        // when
        SellerPayout lowResult = lowBalanceProcessor.process(dto);
        SellerPayout payoutResult = payoutProcessor.process(dto);

        // then: 구조는 동일하되 ID는 다름
        assertThat(lowResult.getSellerId()).isEqualTo(payoutResult.getSellerId());
        assertThat(lowResult.getTotalAmount()).isEqualTo(payoutResult.getTotalAmount());
        assertThat(lowResult.getStatus()).isEqualTo(payoutResult.getStatus());
        assertThat(lowResult.getSellerPayoutId()).isNotEqualTo(payoutResult.getSellerPayoutId());
    }
}
