package store._0982.member.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AddressTest {

    @Test
    @DisplayName("주소 생성 시 필드들이 올바르게 설정된다")
    void create_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");

        // when
        Address address = Address.create(
                member,
                "서울시 강남구",
                "101동 202호",
                "12345",
                "수신인",
                "010-1111-1111"
        );

        // then
        assertThat(address.getAddressId()).isNotNull();
        assertThat(address.getMember()).isEqualTo(member);
        assertThat(address.getAddress()).isEqualTo("서울시 강남구");
        assertThat(address.getAddressDetail()).isEqualTo("101동 202호");
        assertThat(address.getPostalCode()).isEqualTo("12345");
        assertThat(address.getReceiverName()).isEqualTo("수신인");
        assertThat(address.getPhoneNumber()).isEqualTo("010-1111-1111");
        assertThat(address.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("주소 수정 시 필드들이 갱신된다")
    void update_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");
        Address address = Address.create(
                member,
                "서울시 강남구",
                "101동 202호",
                "12345",
                "수신인",
                "010-1111-1111"
        );

        // when
        address.update(
                "서울시 서초구",
                "201동 303호",
                "54321",
                "새 수신인",
                "010-2222-2222"
        );

        // then
        assertThat(address.getAddress()).isEqualTo("서울시 서초구");
        assertThat(address.getAddressDetail()).isEqualTo("201동 303호");
        assertThat(address.getPostalCode()).isEqualTo("54321");
        assertThat(address.getReceiverName()).isEqualTo("새 수신인");
        assertThat(address.getPhoneNumber()).isEqualTo("010-2222-2222");
    }
}

