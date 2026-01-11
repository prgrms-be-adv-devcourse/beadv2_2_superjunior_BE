package store._0982.member.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.auth.Role;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MemberTest {

    @Test
    @DisplayName("회원 생성 시 기본 값들이 올바르게 설정된다")
    void create_success() {
        // given
        String email = "test@example.com";
        String name = "tester";
        String password = "raw-password";
        String phoneNumber = "010-0000-0000";

        // when
        Member member = Member.create(email, name, password, phoneNumber);

        // then
        assertThat(member.getMemberId()).isNotNull();
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getPassword()).isEqualTo(password);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(member.getSaltKey()).isNotNull();
        assertThat(member.getRole()).isEqualTo(Role.CONSUMER);
        assertThat(member.getCreatedAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isNull();
        assertThat(member.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호와 수정 시간이 갱신된다")
    void changePassword_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "old-password", "010-0000-0000");

        // when
        member.changePassword("new-password");

        // then
        assertThat(member.getPassword()).isEqualTo("new-password");
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 인코딩 시 수정 시간은 변경되지 않는다")
    void encodePassword_doesNotChangeUpdatedAt() {
        // given
        Member member = Member.create("test@example.com", "tester", "old-password", "010-0000-0000");
        assertThat(member.getUpdatedAt()).isNull();

        // when
        member.encodePassword("encoded-password");

        // then
        assertThat(member.getPassword()).isEqualTo("encoded-password");
        assertThat(member.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("회원 정보 수정 시 이름, 전화번호와 수정 시간이 갱신된다")
    void update_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");

        // when
        member.update("new-name", "010-1111-1111");

        // then
        assertThat(member.getName()).isEqualTo("new-name");
        assertThat(member.getPhoneNumber()).isEqualTo("010-1111-1111");
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 삭제 시 삭제 시간과 수정 시간이 설정된다")
    void delete_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");

        // when
        member.delete();

        // then
        assertThat(member.getDeletedAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("판매자로 등록 시 역할과 수정 시간이 변경된다")
    void registerSeller_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-0000-0000");
        assertThat(member.getRole()).isEqualTo(Role.CONSUMER);

        // when
        member.registerSeller();

        // then
        assertThat(member.getRole()).isEqualTo(Role.SELLER);
        assertThat(member.getUpdatedAt()).isNotNull();
    }
}

