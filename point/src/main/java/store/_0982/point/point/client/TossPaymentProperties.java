package store._0982.point.point.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.toss")
public class TossPaymentProperties {

    /**
     * 결제 승인 요청 시 사용되는 secret key.
     */
    @Value("${toss.secret-key}")
    private String secretKey;

    /**
     * 프론트 성공 리다이렉트 URL.
     */
    private String successUrl;

    /**
     * 프론트 실패 리다이렉트 URL.
     */
    private String failUrl;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailUrl() {
        return failUrl;
    }

    public void setFailUrl(String failUrl) {
        this.failUrl = failUrl;
    }
}
