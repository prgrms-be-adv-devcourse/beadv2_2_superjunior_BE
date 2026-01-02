package store._0982.member.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.auth.Role;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SellerTest {

    @Test
    @DisplayName("판매자 생성 시 필드들이 올바르게 설정되고 회원 역할이 SELLER로 변경된다")
    void create_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");

        // when
        Seller seller = Seller.create(
                member,
                "001",
                "1234567890",
                "예금주",
                "123-45-67890"
        );

        // then
        assertThat(seller.getMember()).isEqualTo(member);
        assertThat(seller.getBankCode()).isEqualTo("001");
        assertThat(seller.getAccountNumber()).isEqualTo("1234567890");
        assertThat(seller.getAccountHolder()).isEqualTo("예금주");
        assertThat(seller.getBusinessRegistrationNumber()).isEqualTo("123-45-67890");
        assertThat(seller.getCreatedAt()).isNotNull();
        assertThat(member.getRole()).isEqualTo(Role.SELLER);
    }

    @Test
    @DisplayName("판매자 정보 수정 시 계좌 관련 정보가 갱신된다")
    void update_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");
        Seller seller = Seller.create(
                member,
                "001",
                "1234567890",
                "예금주",
                "123-45-67890"
        );

        // when
        seller.update(
                "002",
                "0987654321",
                "새 예금주",
                "987-65-43210"
        );

        // then
        assertThat(seller.getBankCode()).isEqualTo("002");
        assertThat(seller.getAccountNumber()).isEqualTo("0987654321");
        assertThat(seller.getAccountHolder()).isEqualTo("새 예금주");
        assertThat(seller.getBusinessRegistrationNumber()).isEqualTo("987-65-43210");
    }
}

