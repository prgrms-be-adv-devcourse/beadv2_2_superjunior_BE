package store_0982.dummy_data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }
)
public class DummyDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(DummyDataApplication.class, args);
    }

}
