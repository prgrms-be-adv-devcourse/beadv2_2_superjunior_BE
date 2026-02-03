package store_0982.dummy_data.generate_dummy_obj.product;

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
}
