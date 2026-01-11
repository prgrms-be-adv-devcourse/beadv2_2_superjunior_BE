package store._0982.commerce.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "group-purchase.payment")
@Getter
public class GroupPurchaseProperties {
    private final int timeoutMinutes = 10;
}
