package store._0982.common.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import store._0982.common.HeaderName;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnProperty(
        prefix = "common.auth.internal-token",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InternalApiAutoConfig {
    @Value("${token.internal.secret}")
    private String internalToken;

    @Bean
    public RequestInterceptor internalTokenInterceptor() {
        return template -> template.header(HeaderName.TOKEN, internalToken);
    }
}
