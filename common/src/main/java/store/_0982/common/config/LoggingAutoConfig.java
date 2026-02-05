package store._0982.common.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import store._0982.common.log.LoggingAspect;
import store._0982.common.log.property.LoggingAutoProperties;

@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(
        prefix = "common.logging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(LoggingAutoProperties.class)
public class LoggingAutoConfig {

    @Bean
    public LoggingAspect loggingAspect(LoggingAutoProperties properties) {
        return new LoggingAspect(properties);
    }
}
