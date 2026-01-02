package store._0982.member.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EmailTokenTest {

    @Test
    @DisplayName("이메일 토큰 생성 시 기본 값들이 올바르게 설정된다")
    void create_success() {
        // given
        String email = "test@example.com";

        // when
        EmailToken token = EmailToken.create(email);

        // then
        assertThat(token.getEmailTokenId()).isNotNull();
        assertThat(token.getEmail()).isEqualTo(email);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.isVerified()).isFalse();
        assertThat(token.getCreatedAt()).isNotNull();
        assertThat(token.getExpiredAt()).isAfter(token.getCreatedAt());
    }

    @Test
    @DisplayName("이메일 토큰을 갱신하면 토큰 값과 만료 시간이 변경된다")
    void refresh_success() throws InterruptedException {
        // given
        EmailToken token = EmailToken.create("test@example.com");
        String oldToken = token.getToken();
        OffsetDateTime oldExpiredAt = token.getExpiredAt();

        Thread.sleep(1_100L);
        // when
        token.refresh();

        // then
        assertThat(token.getToken()).isNotEqualTo(oldToken);
        assertThat(token.isVerified()).isFalse();
        assertThat(token.getUpdatedAt()).isNotNull();
        assertThat(token.getExpiredAt()).isAfter(token.getUpdatedAt());
        assertThat(token.getExpiredAt()).isNotEqualTo(oldExpiredAt);
    }

    @Test
    @DisplayName("만료 시간이 지난 토큰은 만료된 것으로 판단한다")
    void isExpired_true_whenPastExpiredAt() throws Exception {
        // given
        EmailToken token = EmailToken.create("test@example.com");
        setField(token, "expiredAt", OffsetDateTime.now().minusMinutes(1));

        // when & then
        assertThat(token.isExpired()).isTrue();
    }

    @Test
    @DisplayName("만료 시간이 지나지 않은 토큰은 만료되지 않은 것으로 판단한다")
    void isExpired_false_whenBeforeExpiredAt() throws Exception {
        // given
        EmailToken token = EmailToken.create("test@example.com");
        setField(token, "expiredAt", OffsetDateTime.now().plusMinutes(1));

        // when & then
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("토큰을 검증하면 검증 상태와 수정 시간이 갱신된다")
    void verify_success() {
        // given
        EmailToken token = EmailToken.create("test@example.com");

        // when
        token.verify();

        // then
        assertThat(token.isVerified()).isTrue();
        assertThat(token.getUpdatedAt()).isNotNull();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
