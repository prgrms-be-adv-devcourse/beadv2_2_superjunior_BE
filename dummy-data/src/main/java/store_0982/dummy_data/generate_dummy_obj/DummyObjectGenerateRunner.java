package store_0982.dummy_data.generate_dummy_obj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.generate_dummy_obj.member.DummyMemberGenerator;
import store_0982.dummy_data.generate_dummy_obj.member.DummySellerGenerator;
import store_0982.dummy_data.generate_dummy_obj.product.DummyProductGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DummyObjectGenerateRunner implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final DummyProductGenerator dummyProductGenerator;
    private final DummyMemberGenerator dummyMemberGenerator;
    private final DummySellerGenerator dummySellerGenerator;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        dummyProductGenerator.generateAndWriteCsv(productCount);
        dummyMemberGenerator.readIdAndWriteMember();
        dummySellerGenerator.readIdAndWriteSeller();
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }
}
