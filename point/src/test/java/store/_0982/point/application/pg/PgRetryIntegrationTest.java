package store._0982.point.application.pg;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.support.BaseIntegrationTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
class PgRetryIntegrationTest extends BaseIntegrationTest {

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private PgTxManager pgTxManager;

    @MockitoBean
    private PgPaymentRepository pgPaymentRepository;

    @Test
    @DisplayName("DB 마킹 중 일시적 예외 발생 시 최대 3번까지 재시도한다")
    void retryTest() {
        // given
        String paymentKey = "test-key";
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000);

        when(pgPaymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(pgPayment));
        when(pgPaymentRepository.findByPaymentKey(paymentKey)).thenThrow(new QueryTimeoutException("DB Timeout"));

        TossPaymentInfo response = FIXTURE_MONKEY.giveMeOne(TossPaymentInfo.class);

        // when & then
        assertThatThrownBy(() -> pgTxManager.markConfirmedPayment(response, orderId, memberId))
                .isInstanceOf(QueryTimeoutException.class);

        // verify
        verify(pgPaymentRepository, times(3)).findByPaymentKey(paymentKey);
    }
}
