package store_0982.dummy_data.generate_dummy_obj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyProductGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DummyObjectGenerateRunner implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final DummyProductGenerator dummyProductGenerator;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        dummyProductGenerator.generateAndWriteCsv(productCount);
    }
}
