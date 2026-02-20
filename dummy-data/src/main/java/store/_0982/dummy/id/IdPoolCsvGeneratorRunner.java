package store._0982.dummy.id;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store._0982.dummy.id.groupPurchaseIdPool.GroupPurchaseIdPoolCsvGenerator;
import store._0982.dummy.id.memberIdPool.MemberIdPoolCsvGenerator;
import store._0982.dummy.id.orderIdPool.OrderIdPoolCsvGenerator;
import store._0982.dummy.id.productIdPool.ProductIdPoolCsvGenerator;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class IdPoolCsvGeneratorRunner implements ApplicationRunner {

    @Value("${dummy-data.member-id-pool.count}")
    private int memberCount;
    @Value("${dummy-data.member-id-pool.path}")
    private String memberOutputPath;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Value("${dummy-data.product-id-pool.path}")
    private String productOutputPath;

    @Value("${dummy-data.group-purchase-id-pool.count}")
    private int groupPurchaseCount;
    @Value("${dummy-data.group-purchase-id-pool.path}")
    private String groupPurchaseOutputPath;

    @Value("${dummy-data.order-id-pool.count}")
    private int orderCount;
    @Value("${dummy-data.order-id-pool.path}")
    private String orderOutputPath;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        generateIfMissing(memberOutputPath, memberCount, IdPoolType.MEMBER);
        generateIfMissing(productOutputPath, productCount, IdPoolType.PRODUCT);
        generateIfMissing(groupPurchaseOutputPath, groupPurchaseCount, IdPoolType.GROUP_PURCHASE);
        generateIfMissing(orderOutputPath, orderCount, IdPoolType.ORDER);
        log.info("ID풀 생성 완료");
    }

    private void generateIfMissing(String outputPath, int count, IdPoolType type) throws Exception {
        Path output = Path.of(outputPath);
        Files.createDirectories(output.getParent());
        if (Files.exists(output)) {
            return;
        }

        if (type == IdPoolType.MEMBER) {
            MemberIdPoolCsvGenerator.generate(output, count);
        } else if (type == IdPoolType.PRODUCT) {
            ProductIdPoolCsvGenerator.generate(output, count);
        } else if (type == IdPoolType.GROUP_PURCHASE) {
            GroupPurchaseIdPoolCsvGenerator.generate(output, count);
        } else if (type == IdPoolType.ORDER){
            OrderIdPoolCsvGenerator.generate(output, count);
        }
    }

    private enum IdPoolType {
        MEMBER,
        PRODUCT,
        GROUP_PURCHASE,
        ORDER
    }
}
