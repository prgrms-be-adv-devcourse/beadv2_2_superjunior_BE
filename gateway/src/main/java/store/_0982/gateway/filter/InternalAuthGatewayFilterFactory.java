package store._0982.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import store._0982.gateway.exception.CustomErrorCode;
import store._0982.gateway.exception.ExceptionHandler;

@Component
public class InternalAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<InternalAuthGatewayFilterFactory.Config> {

    private final String internalToken;

    public InternalAuthGatewayFilterFactory(@Value("${token.internal.secret") String internalToken) {
        super(Config.class);
        this.internalToken = internalToken;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String internalToken = request.getHeaders().getFirst("X-Internal-Token");
            if(internalToken == null || !internalToken.equals(this.internalToken)) return ExceptionHandler.responseException(exchange, CustomErrorCode.FORBIDDEN);
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
