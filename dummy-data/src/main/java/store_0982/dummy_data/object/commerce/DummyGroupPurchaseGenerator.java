package store_0982.dummy_data.object.commerce;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store_0982.dummy_data.constants.DummyDataConstants;
import store_0982.dummy_data.object.commerce.row.GroupPurchaseCsvRow;
import store_0982.dummy_data.object.commerce.row.ProductCsvRow;
import store_0982.dummy_data.util.CsvWriterUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DummyGroupPurchaseGenerator {

    @Value("${dummy-data.group-purchase-id-pool.path}")
    private String groupPurchaseIdPoolPath;
    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.product-dummy.path}")
    private String productDummyPath;
    @Value("${dummy-data.group-purchase-dummy.path}")
    private String groupPurchaseDummyPath;

    public void generateAndWriteCsv(int count) throws IOException {
        List<UUID> groupPurchaseIds = readIds(Path.of(groupPurchaseIdPoolPath), count);
        int requiredMembers = (count + 9) / 10;
        List<UUID> memberIds = readIds(Path.of(memberIdPoolPath), requiredMembers);
        if (memberIds.size() < requiredMembers) {
            throw new IllegalStateException("Not enough member IDs for group purchases. required=" + requiredMembers
                    + ", actual=" + memberIds.size());
        }

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema productSchema = CsvWriterUtil.schemaFor(mapper, ProductCsvRow.class);
        CsvSchema groupPurchaseSchema = CsvWriterUtil.schemaFor(mapper, GroupPurchaseCsvRow.class);

        Path output = Path.of(groupPurchaseDummyPath);

        try (var productReader = Files.newBufferedReader(Path.of(productDummyPath));
             MappingIterator<ProductCsvRow> productIterator =
                     mapper.readerFor(ProductCsvRow.class).with(productSchema).readValues(productReader);
             var writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(groupPurchaseSchema).writeValues(writer)) {

            int index = 0;
            while (productIterator.hasNext() && index < groupPurchaseIds.size()) {
                ProductCsvRow product = productIterator.next();
                UUID sellerId = memberIds.get(index / 10);

                int minQuantity = ThreadLocalRandom.current().nextInt(1, 6);
                int maxQuantity = minQuantity + ThreadLocalRandom.current().nextInt(0, 101);
                OffsetDateTime createdAt = randomCreatedAt();
                OffsetDateTime startDate = randomStartDate();

                sequenceWriter.write(new GroupPurchaseCsvRow(
                        groupPurchaseIds.get(index),
                        minQuantity,
                        maxQuantity,
                        product.name() + " 공동구매",
                        product.description(),
                        discountedPrice(product.price()),
                        GroupPurchaseStatus.SCHEDULED,
                        startDate,
                        randomEndDate(startDate),
                        sellerId,
                        product.productId(),
                        0,
                        0,
                        createdAt,
                        randomUpdatedAt(createdAt),
                        null,
                        null,
                        null,
                        null
                ));
                index++;
            }

            if (index < groupPurchaseIds.size()) {
                throw new IllegalStateException("Not enough product rows for group purchases. required="
                        + groupPurchaseIds.size() + ", actual=" + index);
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

    private OffsetDateTime randomStartDate() {
        int daysForward = ThreadLocalRandom.current().nextInt(0, 31);
        int secondsForward = ThreadLocalRandom.current().nextInt(0, 24 * 60 * 60);
        return OffsetDateTime.now().plusDays(daysForward).plusSeconds(secondsForward);
    }

    private OffsetDateTime randomEndDate(OffsetDateTime startDate) {
        int daysForward = ThreadLocalRandom.current().nextInt(1, 31);
        int secondsForward = ThreadLocalRandom.current().nextInt(0, 24 * 60 * 60);
        return startDate.plusDays(daysForward).plusSeconds(secondsForward);
    }

    private long discountedPrice(long originalPrice) {
        int discountRate = ThreadLocalRandom.current().nextInt(
                (int) (DummyDataConstants.GROUP_PURCHASE_MIN_DISCOUNT * 100),
                (int) (DummyDataConstants.GROUP_PURCHASE_MAX_DISCOUNT * 100) + 1
        );
        return originalPrice * (100 - discountRate) / 100;
    }
}
