package store._0982.common.log.property;

import org.springframework.boot.context.properties.bind.DefaultValue;

public record ControllerProperties(
        @DefaultValue("true")
        boolean enabled
) {
}
