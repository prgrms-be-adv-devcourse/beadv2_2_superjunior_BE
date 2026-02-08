package store._0982.batch.domain.grouppurchase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GroupPurchase 도메인 단위 테스트")
class GroupPurchaseTest {

    @Test
    @DisplayName("공동구매 생성 시 초기 상태는 SCHEDULED이다")
    void create_shouldHaveScheduledStatus() {
        // given & when
        GroupPurchase groupPurchase = createGroupPurchase();

        // then
        assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.SCHEDULED);
        assertThat(groupPurchase.getCurrentQuantity()).isZero();
    }

    @Nested
    @DisplayName("open() 메서드")
    class Open {

        @Test
        @DisplayName("SCHEDULED 상태에서 OPEN으로 변경할 수 있다")
        void open_fromScheduled_shouldChangeToOpen() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();

            // when
            groupPurchase.open();

            // then
            assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.OPEN);
        }

        @Test
        @DisplayName("OPEN 상태에서 open() 호출 시 예외가 발생한다")
        void open_fromOpen_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();

            // when & then
            assertThatThrownBy(groupPurchase::open)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SCHEDULED 일 때만 변경 가능");
        }

        @Test
        @DisplayName("SUCCESS 상태에서 open() 호출 시 예외가 발생한다")
        void open_fromSuccess_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();
            groupPurchase.markSuccess();

            // when & then
            assertThatThrownBy(groupPurchase::open)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SCHEDULED 일 때만 변경 가능");
        }

        @Test
        @DisplayName("FAILED 상태에서 open() 호출 시 예외가 발생한다")
        void open_fromFailed_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();
            groupPurchase.markFailed();

            // when & then
            assertThatThrownBy(groupPurchase::open)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SCHEDULED 일 때만 변경 가능");
        }
    }

    @Nested
    @DisplayName("markSuccess() 메서드")
    class MarkSuccess {

        @Test
        @DisplayName("OPEN 상태에서 SUCCESS로 변경할 수 있다")
        void markSuccess_fromOpen_shouldChangeToSuccess() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();

            // when
            groupPurchase.markSuccess();

            // then
            assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
            assertThat(groupPurchase.getSucceededAt()).isNotNull();
        }

        @Test
        @DisplayName("SUCCESS 변경 시 succeededAt이 설정된다")
        void markSuccess_shouldSetSucceededAt() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();
            OffsetDateTime before = OffsetDateTime.now();

            // when
            groupPurchase.markSuccess();

            // then
            assertThat(groupPurchase.getSucceededAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("SCHEDULED 상태에서 markSuccess() 호출 시 예외가 발생한다")
        void markSuccess_fromScheduled_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();

            // when & then
            assertThatThrownBy(groupPurchase::markSuccess)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("OPEN 일 때만 변경 가능");
        }

        @Test
        @DisplayName("FAILED 상태에서 markSuccess() 호출 시 예외가 발생한다")
        void markSuccess_fromFailed_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();
            groupPurchase.markFailed();

            // when & then
            assertThatThrownBy(groupPurchase::markSuccess)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("OPEN 일 때만 변경 가능");
        }
    }

    @Nested
    @DisplayName("markFailed() 메서드")
    class MarkFailed {

        @Test
        @DisplayName("OPEN 상태에서 FAILED로 변경할 수 있다")
        void markFailed_fromOpen_shouldChangeToFailed() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();

            // when
            groupPurchase.markFailed();

            // then
            assertThat(groupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.FAILED);
        }

        @Test
        @DisplayName("SCHEDULED 상태에서 markFailed() 호출 시 예외가 발생한다")
        void markFailed_fromScheduled_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();

            // when & then
            assertThatThrownBy(groupPurchase::markFailed)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("OPEN 일 때만 변경 가능");
        }

        @Test
        @DisplayName("SUCCESS 상태에서 markFailed() 호출 시 예외가 발생한다")
        void markFailed_fromSuccess_shouldThrowException() {
            // given
            GroupPurchase groupPurchase = createGroupPurchase();
            groupPurchase.open();
            groupPurchase.markSuccess();

            // when & then
            assertThatThrownBy(groupPurchase::markFailed)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("OPEN 일 때만 변경 가능");
        }
    }

    private GroupPurchase createGroupPurchase() {
        return new GroupPurchase(
                10,
                100,
                "테스트 공동구매",
                "테스트 설명",
                10000L,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(7),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
        );
    }
}
