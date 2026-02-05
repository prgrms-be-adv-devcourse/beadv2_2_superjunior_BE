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
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyOrderGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyProductGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DummyObjectGenerateRunner implements ApplicationRunner {

    private final DummyProductGenerator dummyProductGenerator;
    private final DummyMemberGenerator dummyMemberGenerator;
    private final DummySellerGenerator dummySellerGenerator;
    private final DummyGroupPurchaseGenerator dummyGroupPurchaseGenerator;
    private final DummyOrderGenerator dummyOrderGenerator;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Value("${dummy-data.group-purchase-id-pool.count}")
    private int groupPurchaseCount;
    @Value("${dummy-data.order-id-pool.count}")
    private int orderCount;
    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        dummyProductGenerator.generateAndWriteCsv(productCount);
        dummyGroupPurchaseGenerator.generateAndWriteCsv(groupPurchaseCount);
        dummyMemberGenerator.readIdAndWriteMember();
        dummySellerGenerator.readIdAndWriteSeller();
        dummyOrderGenerator.generateAndWriteCsv(orderCount);
    }
}
