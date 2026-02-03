package store_0982.dummy_data.generateIdPool;

import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.generateIdPool.groupPurchaseIdPool.GroupPurchaseIdPoolCsvGenerator;
import store_0982.dummy_data.generateIdPool.memberIdPool.MemberIdPoolCsvGenerator;
import store_0982.dummy_data.generateIdPool.orderIdPool.OrderIdPoolCsvGenerator;
import store_0982.dummy_data.generateIdPool.productIdPool.ProductIdPoolCsvGenerator;

@Component
@RequiredArgsConstructor
public class IdPoolCsvGeneratorRunner implements ApplicationRunner {

    private final ApplicationContext applicationContext;

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

        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
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
        } else {
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
