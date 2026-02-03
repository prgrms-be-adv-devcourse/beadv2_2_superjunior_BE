package store._0982.common.log.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "common.logging")
public class LoggingAutoProperties {

    private final ControllerProperties controller;
    private final ServiceProperties service;
}
