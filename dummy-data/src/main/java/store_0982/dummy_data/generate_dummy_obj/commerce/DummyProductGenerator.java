package store_0982.dummy_data.generate_dummy_obj.commerce;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.datafaker.Faker;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.product.Product;
import store._0982.common.domain.product.ProductCategory;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.ProductCsvRow;
import store_0982.dummy_data.util.Utils;

@Component
public class DummyProductGenerator {

    private final Faker faker = new Faker(new Locale("ko", "ko"));

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
                    ProductCategory category = product.getCategory();
                    if (category == null) {
                        category = ProductCategory.values()[ThreadLocalRandom.current().nextInt(ProductCategory.values().length)];
                        Utils.setField(product, "category", category);
                    }
                    Utils.setField(product, "name", buildName(category));
                    Utils.setField(product, "description", buildDescription(category));
                    Utils.setField(product, "idempotencyKey", UUID.randomUUID().toString());
                    OffsetDateTime createdAt = randomCreatedAt();
                    OffsetDateTime updatedAt = randomUpdatedAt(createdAt);
                    Utils.setField(product, "createdAt", createdAt);
                    Utils.setField(product, "updatedAt", updatedAt);
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
            writer.write('\uFEFF');
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

    private OffsetDateTime randomCreatedAt() {
        int daysBack = ThreadLocalRandom.current().nextInt(0, 365);
        int secondsBack = ThreadLocalRandom.current().nextInt(0, 24 * 60 * 60);
        return OffsetDateTime.now().minusDays(daysBack).minusSeconds(secondsBack);
    }

    private OffsetDateTime randomUpdatedAt(OffsetDateTime createdAt) {
        int secondsForward = ThreadLocalRandom.current().nextInt(0, 30 * 24 * 60 * 60);
        return createdAt.plusSeconds(secondsForward);
    }

    private String buildName(ProductCategory category) {
        return switch (category) {
            case HOME -> "생활용품 " + faker.commerce().productName();
            case FOOD -> "식품 " + faker.commerce().productName();
            case HEALTH -> "건강 " + faker.commerce().productName();
            case BEAUTY -> "뷰티 " + faker.commerce().productName();
            case FASHION -> "패션 " + faker.commerce().productName();
            case ELECTRONICS -> "전자 " + faker.commerce().productName();
            case KIDS -> "키즈 " + faker.commerce().productName();
            case HOBBY -> "취미 " + faker.commerce().productName();
            case PET -> "반려 " + faker.commerce().productName();
        };
    }

    private String buildDescription(ProductCategory category) {
        String prefix = switch (category) {
            case HOME -> "집안에서 유용한 ";
            case FOOD -> "신선한 재료로 만든 ";
            case HEALTH -> "건강을 챙기는 ";
            case BEAUTY -> "피부와 스타일을 위한 ";
            case FASHION -> "데일리로 활용하기 좋은 ";
            case ELECTRONICS -> "실용적인 기능을 갖춘 ";
            case KIDS -> "아이들을 위한 ";
            case HOBBY -> "취미 생활에 딱 맞는 ";
            case PET -> "반려동물을 위한 ";
        };
        return prefix + faker.lorem().sentence();
    }
}
