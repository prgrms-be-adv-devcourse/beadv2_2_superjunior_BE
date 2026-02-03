package store_0982.dummy_data.generate_dummy_obj.product;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.product.Product;
import store_0982.dummy_data.util.Utils;

@Component
public class DummyProductGenerator {

    @Value("${dummy-data.product-id-pool.path}")
    private String productIdPoolPath;
    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.product-dummy.path}")
    private String productDummyPath;

    public List<Product> generate(int count) throws IOException {
        EasyRandomParameters parameters = new EasyRandomParameters();
        EasyRandom easyRandom = new EasyRandom(parameters);

        List<UUID> productIds = readIds(Path.of(productIdPoolPath), count);
        int requiredMembers = (count + 1) / 2;
        List<UUID> memberIds = readIds(Path.of(memberIdPoolPath), requiredMembers);
        if (memberIds.size() < requiredMembers) {
            throw new IllegalStateException("Not enough member IDs for products. required=" + requiredMembers
                    + ", actual=" + memberIds.size());
        }

        return IntStream.range(0, productIds.size())
                .mapToObj(i -> {
                    Product product = easyRandom.nextObject(Product.class);
                    Utils.setField(product, "productId", productIds.get(i));
                    Utils.setField(product, "sellerId", memberIds.get(i / 2));
                    return product;
                })
                .toList();
    }

    public void generateAndWriteCsv(int count) throws IOException {
        List<Product> products = generate(count);
        writeProductCsv(products, Path.of(productDummyPath));
    }

    private List<UUID> readIds(Path path, int count) throws IOException {
        try (var lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .limit(count)
                    .map(UUID::fromString)
                    .toList();
        }
    }

    private void writeProductCsv(List<Product> products, Path output) throws IOException {
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
