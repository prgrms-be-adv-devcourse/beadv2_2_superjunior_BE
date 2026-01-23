package store._0982.point.domain;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "payment.rules")
public class PaymentRules {

    @Positive(message = "환불 가능 일수는 1 이상이어야 합니다.")
    private final int refundDays;
}
