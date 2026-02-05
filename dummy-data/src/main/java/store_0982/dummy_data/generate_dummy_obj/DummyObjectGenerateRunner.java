package store_0982.dummy_data.generate_dummy_obj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.generate_dummy_obj.member.DummyMemberGenerator;
import store_0982.dummy_data.generate_dummy_obj.member.DummySellerGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyGroupPurchaseGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyProductGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DummyObjectGenerateRunner implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final DummyProductGenerator dummyProductGenerator;
    private final DummyMemberGenerator dummyMemberGenerator;
    private final DummySellerGenerator dummySellerGenerator;
    private final DummyGroupPurchaseGenerator dummyGroupPurchaseGenerator;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Value("${dummy-data.group-purchase-id-pool.count}")
    private int groupPurchaseCount;
    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        dummyProductGenerator.generateAndWriteCsv(productCount);
        dummyMemberGenerator.readIdAndWriteMember();
        dummySellerGenerator.readIdAndWriteSeller();
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
        log.info("상품 더미데이터 생성 완료");
        dummyGroupPurchaseGenerator.generateAndWriteCsv(groupPurchaseCount);
        log.info("공동구매 더미데이터 생성 완료");
    }
}
