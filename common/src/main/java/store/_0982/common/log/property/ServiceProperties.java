package store._0982.common.log.property;

import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record ServiceProperties(
        @DefaultValue("true")
        boolean enabled,

        @PositiveOrZero(message = "느린 실행 시간 임계값은 비어 있거나, 0 이상의 값이어야 합니다. (ms 단위)")
        Integer slowThresholdMs
) {
}
