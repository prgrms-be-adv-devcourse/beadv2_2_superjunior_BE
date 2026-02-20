package store._0982.point;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.retry.annotation.EnableRetry;
import store._0982.point.domain.PaymentRules;

@SpringBootApplication
@EnableFeignClients
@EnableRetry
@EnableConfigurationProperties(PaymentRules.class)
@EntityScan(basePackages = "store._0982.point.domain")
public class PointApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointApplication.class, args);
    }

}
