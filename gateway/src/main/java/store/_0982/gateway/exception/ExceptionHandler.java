package store._0982.gateway.exception;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class ExceptionHandler {

    @Value("${frontend.url}")
    private String frontendUrl;

    public Mono<Void> responseException(ServerWebExchange exchange, CustomErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(errorCode.getHttpStatus());
        response.getHeaders().set("Access-Control-Allow-Origin", frontendUrl);
        response.getHeaders().set("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Vary", "Origin");
        response.getHeaders().remove("Www-Authenticate");
        byte[] bytes = errorCode.getMessage().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
