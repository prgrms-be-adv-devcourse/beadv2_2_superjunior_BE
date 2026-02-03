package store_0982.dummy_data.generate_dummy_obj;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store._0982.common.domain.product.Product;
import store_0982.dummy_data.generate_dummy_obj.product.DummyProductGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DummyObjectGenerateRunner implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final DummyProductGenerator dummyProductGenerator;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Value("${dummy-data.product-dummy.path}")
    private String productDummyPath;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        var products = dummyProductGenerator.generate(productCount);
        writeProductCsv(products, Path.of(productDummyPath));
        log.info("Generated dummy products. count={}, path={}", products.size(), Path.of(productDummyPath).toAbsolutePath());
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    private void writeProductCsv(List<Product> products, Path output) throws Exception {
        Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("product_id,name,price,category,description,stock,original_url,seller_id,created_at,updated_at,deleted_at,idempotency_key,image_url");
            writer.newLine();

            for (Product product : products) {
                writer.write(toCsvRow(product));
                writer.newLine();
            }
        }
    }

    private String toCsvRow(Product product) {
        return String.join(",",
                escape(product.getProductId()),
                escape(product.getName()),
                escape(product.getPrice()),
                escape(product.getCategory()),
                escape(product.getDescription()),
                escape(product.getStock()),
                escape(product.getOriginalUrl()),
                escape(product.getSellerId()),
                escape(product.getCreatedAt()),
                escape(product.getUpdatedAt()),
                escape(product.getDeletedAt()),
                escape(product.getIdempotencyKey()),
                escape(product.getImageUrl())
        );
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains("\"") || text.contains(",") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
