package store._0982.common.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import store._0982.common.auth.RoleCheckAspect;

@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(
        prefix = "common.auth",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AuthAutoConfig {
    @Bean
    public RoleCheckAspect roleCheckAspect() {
        return new RoleCheckAspect();
    }
}
