package store._0982.point;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PointApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointApplication.class, args);
    }

}
