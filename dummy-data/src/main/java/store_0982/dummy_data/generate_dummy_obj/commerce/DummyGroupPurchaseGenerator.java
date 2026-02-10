package store_0982.dummy_data.generate_dummy_obj.commerce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.GroupPurchaseCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.ProductCsvRow;
import store_0982.dummy_data.util.Utils;

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

        EasyRandomParameters parameters = new EasyRandomParameters();
        EasyRandom easyRandom = new EasyRandom(parameters);

        CsvMapper productMapper = new CsvMapper();
        productMapper.findAndRegisterModules();
        productMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        productMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema productSchema = productMapper.schemaFor(ProductCsvRow.class).withHeader();

        CsvMapper groupPurchaseMapper = new CsvMapper();
        groupPurchaseMapper.findAndRegisterModules();
        groupPurchaseMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        groupPurchaseMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema groupPurchaseSchema = groupPurchaseMapper.schemaFor(GroupPurchaseCsvRow.class).withHeader();

        Path output = Path.of(groupPurchaseDummyPath);
        Files.createDirectories(output.getParent());

        try (var productReader = Files.newBufferedReader(Path.of(productDummyPath));
             MappingIterator<ProductCsvRow> productIterator =
                     productMapper.readerFor(ProductCsvRow.class).with(productSchema).readValues(productReader);
             var groupPurchaseWriter = Files.newBufferedWriter(output);
             var sequenceWriter = groupPurchaseMapper.writer(groupPurchaseSchema).writeValues(groupPurchaseWriter)) {

            groupPurchaseWriter.write('\uFEFF');
            int index = 0;
            while (productIterator.hasNext() && index < groupPurchaseIds.size()) {
                ProductCsvRow productRow = productIterator.next();
                GroupPurchase groupPurchase = easyRandom.nextObject(GroupPurchase.class);
                Utils.setField(groupPurchase, "groupPurchaseId", groupPurchaseIds.get(index));
                Utils.setField(groupPurchase, "version", 0L);
                Utils.setField(groupPurchase, "sellerId", memberIds.get(index / 10));
                Utils.setField(groupPurchase, "productId", productRow.productId());

                int minQuantity = ThreadLocalRandom.current().nextInt(1, 6);
                int maxQuantity = minQuantity + ThreadLocalRandom.current().nextInt(0, 101);
                Utils.setField(groupPurchase, "minQuantity", minQuantity);
                Utils.setField(groupPurchase, "maxQuantity", maxQuantity);
                Utils.setField(groupPurchase, "currentQuantity", 0);
                Utils.setField(groupPurchase, "likeCount", 0);

                Utils.setField(groupPurchase, "title", productRow.name() + " 공동구매");
                Utils.setField(groupPurchase, "description", productRow.description());
                Utils.setField(groupPurchase, "discountedPrice", discountedPrice(productRow.price()));

                OffsetDateTime createdAt = randomCreatedAt();
                OffsetDateTime updatedAt = randomUpdatedAt(createdAt);
                Utils.setField(groupPurchase, "createdAt", createdAt);
                Utils.setField(groupPurchase, "updatedAt", updatedAt);

                OffsetDateTime startDate = randomStartDate();
                OffsetDateTime endDate = randomEndDate(startDate);
                Utils.setField(groupPurchase, "startDate", startDate);
                Utils.setField(groupPurchase, "endDate", endDate);

                Utils.setField(groupPurchase, "status", GroupPurchaseStatus.SCHEDULED);
                Utils.setField(groupPurchase, "settledAt", null);
                Utils.setField(groupPurchase, "returnedAt", null);
                Utils.setField(groupPurchase, "succeededAt", null);

                sequenceWriter.write(toCsvRow(groupPurchase));
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
                    .toList();
        }
    }

    private GroupPurchaseCsvRow toCsvRow(GroupPurchase groupPurchase) {
        return new GroupPurchaseCsvRow(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getVersion(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getTitle(),
                groupPurchase.getDescription(),
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getStatus(),
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                groupPurchase.getSellerId(),
                groupPurchase.getProductId(),
                groupPurchase.getCurrentQuantity(),
                groupPurchase.getLikeCount(),
                groupPurchase.getCreatedAt(),
                groupPurchase.getUpdatedAt(),
                groupPurchase.getSettledAt(),
                groupPurchase.getReturnedAt(),
                groupPurchase.getSucceededAt(),
                groupPurchase.getImageUrl()
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
        int discountRate = ThreadLocalRandom.current().nextInt(10, 31);
        return originalPrice * (100 - discountRate) / 100;
    }
}
