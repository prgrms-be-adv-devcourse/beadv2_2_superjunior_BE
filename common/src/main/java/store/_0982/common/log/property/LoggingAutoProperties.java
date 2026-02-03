package store._0982.common.log.property;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "common.logging")
public class LoggingAutoProperties {

    @Valid
    @NestedConfigurationProperty
    private ControllerProperties controller = new ControllerProperties(true);

    @Valid
    @NestedConfigurationProperty
    private final ServiceProperties service = new ServiceProperties(true, null);
}
