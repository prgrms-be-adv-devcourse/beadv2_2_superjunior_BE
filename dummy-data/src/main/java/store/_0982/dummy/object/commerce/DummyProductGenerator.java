package store._0982.dummy.object.commerce;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.product.ProductCategory;
import store._0982.dummy.constants.DummyDataConstants;
import store._0982.dummy.object.commerce.row.ProductCsvRow;
import store._0982.dummy.util.CsvWriterUtil;
import store._0982.dummy.util.ProductTextProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DummyProductGenerator {

    @Value("${dummy-data.product-id-pool.path}")
    private String productIdPoolPath;
    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.product-dummy.path}")
    private String productDummyPath;
    @Value("${dummy-data.category.pet-percent}")
    private int petPercent;

    public void generateAndWriteCsv(int count) throws IOException {
        List<UUID> productIds = readIds(Path.of(productIdPoolPath), count);
        int requiredMembers = (count + 9) / 10;
        List<UUID> memberIds = readIds(Path.of(memberIdPoolPath), requiredMembers);
        if (memberIds.size() < requiredMembers) {
            throw new IllegalStateException("Not enough member IDs for products. required=" + requiredMembers
                    + ", actual=" + memberIds.size());
        }

        Path output = Path.of(productDummyPath);
        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, ProductCsvRow.class);

        try (var writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            for (int i = 0; i < productIds.size(); i++) {
                UUID sellerId = memberIds.get(i / 10);
                ProductCategory category = pickCategory();
                OffsetDateTime createdAt = randomCreatedAt();

                sequenceWriter.write(new ProductCsvRow(
                        productIds.get(i),
                        ProductTextProvider.name(category),
                        ThreadLocalRandom.current().nextLong(DummyDataConstants.PRODUCT_MIN_PRICE, DummyDataConstants.PRODUCT_MAX_PRICE + 1),
                        category,
                        ProductTextProvider.description(category),
                        ThreadLocalRandom.current().nextInt(DummyDataConstants.PRODUCT_MIN_STOCK, DummyDataConstants.PRODUCT_MAX_STOCK + 1),
                        null,
                        sellerId,
                        createdAt,
                        randomUpdatedAt(createdAt),
                        null,
                        UUID.randomUUID().toString(),
                        null
                ));
            }
        }
    }

    private List<UUID> readIds(Path path, int count) throws IOException {
        try (var lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .limit(count)
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }
    }

    private OffsetDateTime randomCreatedAt() {
        int daysBack = ThreadLocalRandom.current().nextInt(0, DummyDataConstants.PRODUCT_CREATED_DAYS_RANGE);
        int secondsBack = ThreadLocalRandom.current().nextInt(0, 24 * 60 * 60);
        return OffsetDateTime.now().minusDays(daysBack).minusSeconds(secondsBack);
    }

    private OffsetDateTime randomUpdatedAt(OffsetDateTime createdAt) {
        int secondsForward = ThreadLocalRandom.current().nextInt(0, DummyDataConstants.PRODUCT_UPDATED_DAYS_RANGE * 24 * 60 * 60);
        return createdAt.plusSeconds(secondsForward);
    }

    private ProductCategory pickCategory() {
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < petPercent) {
            return ProductCategory.PET;
        }
        ProductCategory[] categories = ProductCategory.values();
        ProductCategory pick;
        do {
            pick = categories[ThreadLocalRandom.current().nextInt(categories.length)];
        } while (pick == ProductCategory.PET);
        return pick;
    }
}
