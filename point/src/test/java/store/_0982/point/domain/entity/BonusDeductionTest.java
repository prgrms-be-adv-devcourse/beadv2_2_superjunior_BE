package store._0982.point.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BonusDeductionTest {

    @Test
    @DisplayName("보너스 차감 내역을 생성한다")
    void create() {
        // given
        UUID bonusEarningId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        long amount = 2000;

        // when
        BonusDeduction deduction = BonusDeduction.create(bonusEarningId, transactionId, amount);

        // then
        assertThat(deduction.getBonusEarningId()).isEqualTo(bonusEarningId);
        assertThat(deduction.getTransactionId()).isEqualTo(transactionId);
        assertThat(deduction.getAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("보너스 차감 내역을 여러 개 생성한다")
    void createMultiple() {
        // given
        UUID bonusEarningId = UUID.randomUUID();
        UUID transactionId1 = UUID.randomUUID();
        UUID transactionId2 = UUID.randomUUID();

        // when
        BonusDeduction deduction1 = BonusDeduction.create(bonusEarningId, transactionId1, 1000);
        BonusDeduction deduction2 = BonusDeduction.create(bonusEarningId, transactionId2, 1500);

        // then
        assertThat(deduction1.getBonusEarningId()).isEqualTo(bonusEarningId);
        assertThat(deduction1.getAmount()).isEqualTo(1000);
        assertThat(deduction2.getBonusEarningId()).isEqualTo(bonusEarningId);
        assertThat(deduction2.getAmount()).isEqualTo(1500);
    }
}
