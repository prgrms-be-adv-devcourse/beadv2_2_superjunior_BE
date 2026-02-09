package store._0982.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store._0982.common.domain.order.OrderStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Nested
    @DisplayName("주문번호 생성 테스트")
    class OrderNumberGenerationTest {

        @Test
        @DisplayName("주문번호 형식이 올바르게 생성된다")
        void generateOrderNumber_format() {
            // given
            String expectedDatePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            Pattern pattern = Pattern.compile("^ORD-" + expectedDatePart + "-[A-Z0-9]{8}$");

            // when
            String orderNumber = Order.generateOrderNumber();

            // then
            assertThat(orderNumber).matches(pattern);
        }

        @Test
        @DisplayName("주문번호가 ORD- 접두사로 시작한다")
        void generateOrderNumber_prefix() {
            // when
            String orderNumber = Order.generateOrderNumber();

            // then
            assertThat(orderNumber).startsWith("ORD-");
        }

        @Test
        @DisplayName("주문번호에 오늘 날짜가 포함된다")
        void generateOrderNumber_containsDate() {
            // given
            String expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));

            // when
            String orderNumber = Order.generateOrderNumber();

            // then
            assertThat(orderNumber).contains(expectedDate);
        }

        @Test
        @DisplayName("주문번호의 랜덤 부분은 8자리이다")
        void generateOrderNumber_randomPartLength() {
            // when
            String orderNumber = Order.generateOrderNumber();
            String[] parts = orderNumber.split("-");

            // then
            assertThat(parts).hasSize(3);
            assertThat(parts[2]).hasSize(8);
        }

        @Test
        @DisplayName("여러 번 생성해도 중복되지 않는다")
        void generateOrderNumber_noDuplicate() {
            // given
            int count = 1000;
            Set<String> orderNumbers = new HashSet<>();

            // when
            for (int i = 0; i < count; i++) {
                orderNumbers.add(Order.generateOrderNumber());
            }

            // then
            assertThat(orderNumbers).hasSize(count);
        }

        @Test
        @DisplayName("랜덤 부분은 대문자와 숫자로만 구성된다")
        void generateOrderNumber_alphanumericOnly() {
            // when
            String orderNumber = Order.generateOrderNumber();
            String randomPart = orderNumber.split("-")[2];

            // then
            assertThat(randomPart).matches("^[A-Z0-9]+$");
        }
    }

    @Nested
    @DisplayName("Order 생성 테스트")
    class OrderCreateTest {

        @Test
        @DisplayName("Order 생성 시 주문번호가 자동으로 생성된다")
        void create_generatesOrderNumber() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            UUID groupPurchaseId = UUID.randomUUID();

            // when
            Order order = Order.create(
                    2,
                    10000L,
                    memberId,
                    "서울시 강남구",
                    "101호",
                    "12345",
                    "홍길동",
                    sellerId,
                    groupPurchaseId,
                    "idempotency-key-123"
            );

            // then
            assertThat(order.getOrderNumber()).isNotNull();
            assertThat(order.getOrderNumber()).startsWith("ORD-");
        }

        @Test
        @DisplayName("Order 생성 시 상태는 PENDING이다")
        void create_statusIsPending() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            UUID groupPurchaseId = UUID.randomUUID();

            // when
            Order order = Order.create(
                    2,
                    10000L,
                    memberId,
                    "서울시 강남구",
                    "101호",
                    "12345",
                    "홍길동",
                    sellerId,
                    groupPurchaseId,
                    "idempotency-key-123"
            );

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Order 생성 시 orderId가 자동으로 생성된다")
        void create_generatesOrderId() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            UUID groupPurchaseId = UUID.randomUUID();

            // when
            Order order = Order.create(
                    2,
                    10000L,
                    memberId,
                    "서울시 강남구",
                    "101호",
                    "12345",
                    "홍길동",
                    sellerId,
                    groupPurchaseId,
                    "idempotency-key-123"
            );

            // then
            assertThat(order.getOrderId()).isNotNull();
        }

        @Test
        @DisplayName("Order 생성 시 만료 시간이 설정된다")
        void create_setsExpiredAt() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            UUID groupPurchaseId = UUID.randomUUID();

            // when
            Order order = Order.create(
                    2,
                    10000L,
                    memberId,
                    "서울시 강남구",
                    "101호",
                    "12345",
                    "홍길동",
                    sellerId,
                    groupPurchaseId,
                    "idempotency-key-123"
            );

            // then
            assertThat(order.getExpiredAt()).isNotNull();
        }
    }
}
