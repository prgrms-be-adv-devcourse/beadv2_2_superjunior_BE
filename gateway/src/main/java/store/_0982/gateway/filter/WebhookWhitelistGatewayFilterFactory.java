package store._0982.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.List;

@Component
public class WebhookWhitelistGatewayFilterFactory
        extends AbstractGatewayFilterFactory<WebhookWhitelistGatewayFilterFactory.Config> {

    public WebhookWhitelistGatewayFilterFactory() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String ip = extractClientIp(exchange);
            if (ip != null && isAllowed(ip, config.cidrs)) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        };
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : null;
    }

    private boolean isAllowed(String ip, List<String> cidrs) {
        return cidrs != null && cidrs.stream().anyMatch(cidr -> matchesCidr(ip, cidr));
    }

    /**
     * Simple IPv4/IPv6 CIDR matcher that does not rely on servlet APIs.
     */
    private boolean matchesCidr(String ip, String cidr) {
        try {
            // 단일 IP 허용
            if (!cidr.contains("/")) {
                return InetAddress.getByName(ip).equals(InetAddress.getByName(cidr));
            }

            String[] parts = cidr.split("/");
            InetAddress targetAddr = InetAddress.getByName(ip);
            InetAddress baseAddr = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] targetBytes = targetAddr.getAddress();
            byte[] baseBytes = baseAddr.getAddress();

            // IPv4/IPv6 길이가 다른 경우 매칭 실패
            if (targetBytes.length != baseBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (targetBytes[i] != baseBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = 0xFF << (8 - remainingBits);
                return (targetBytes[fullBytes] & mask) == (baseBytes[fullBytes] & mask);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class Config {
        private List<String> cidrs;
        public List<String> getCidrs() { return cidrs; }
        public void setCidrs(List<String> cidrs) { this.cidrs = cidrs; }
    }
}
