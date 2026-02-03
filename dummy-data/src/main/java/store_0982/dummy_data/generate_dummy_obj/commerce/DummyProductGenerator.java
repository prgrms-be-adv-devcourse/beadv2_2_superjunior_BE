package store_0982.dummy_data.generate_dummy_obj.commerce;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.product.Product;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.ProductCsvRow;
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
        EasyRandomParameters parameters = new EasyRandomParameters()
                .randomize(
                        (Field field) -> field.getName().equals("price")
                                && field.getDeclaringClass().equals(Product.class),
                        (Randomizer<Long>) () -> ThreadLocalRandom.current().nextLong(0, 1_000_001)
                )
                .randomize(
                        (Field field) -> field.getName().equals("stock")
                                && field.getDeclaringClass().equals(Product.class),
                        (Randomizer<Integer>) () -> ThreadLocalRandom.current().nextInt(1, 10_001)
                );
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
                    Utils.setField(product, "deletedAt", null);
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
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(ProductCsvRow.class).withHeader();

        try (var writer = Files.newBufferedWriter(output);
             var sequenceWriter = mapper.writer(schema).writeValues(writer)) {
            for (Product product : products) {
                sequenceWriter.write(toCsvRow(product));
            }
        }
    }

    private ProductCsvRow toCsvRow(Product product) {
        return new ProductCsvRow(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription(),
                product.getStock(),
                product.getOriginalUrl(),
                product.getSellerId(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getDeletedAt(),
                product.getIdempotencyKey(),
                product.getImageUrl()
        );
    }
}
