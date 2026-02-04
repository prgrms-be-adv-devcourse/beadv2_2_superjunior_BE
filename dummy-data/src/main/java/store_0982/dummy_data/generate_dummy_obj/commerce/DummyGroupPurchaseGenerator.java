package store_0982.dummy_data.generate_dummy_obj.commerce;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
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
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.GroupPurchaseCsvRow;
import store_0982.dummy_data.util.Utils;

@Component
public class DummyGroupPurchaseGenerator {

    @Value("${dummy-data.group-purchase-id-pool.path}")
    private String groupPurchaseIdPoolPath;
    @Value("${dummy-data.product-id-pool.path}")
    private String productIdPoolPath;
    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.group-purchase-dummy.path}")
    private String groupPurchaseDummyPath;

    public void generateAndWriteCsv(int count) throws IOException {
        List<GroupPurchase> groupPurchases = generate(count);
        writeGroupPurchaseCsv(groupPurchases, Path.of(groupPurchaseDummyPath));
    }

    public List<GroupPurchase> generate(int count) throws IOException {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .randomize(
                        (Field field) -> field.getName().equals("discountedPrice")
                                && field.getDeclaringClass().equals(GroupPurchase.class),
                        (Randomizer<Long>) () -> ThreadLocalRandom.current().nextLong(0, 1_000_001)
                );
        EasyRandom easyRandom = new EasyRandom(parameters);

        List<UUID> groupPurchaseIds = readIds(Path.of(groupPurchaseIdPoolPath), count);
        List<UUID> productIds = readIds(Path.of(productIdPoolPath), count);
        int requiredMembers = (count + 1) / 2;
        List<UUID> memberIds = readIds(Path.of(memberIdPoolPath), requiredMembers);
        if (memberIds.size() < requiredMembers) {
            throw new IllegalStateException("Not enough member IDs for group purchases. required=" + requiredMembers
                    + ", actual=" + memberIds.size());
        }

                return IntStream.range(0, groupPurchaseIds.size())
                .mapToObj(i -> {
                    GroupPurchase groupPurchase = easyRandom.nextObject(GroupPurchase.class);
                    Utils.setField(groupPurchase, "groupPurchaseId", groupPurchaseIds.get(i));
                    Utils.setField(groupPurchase, "version", 0L);
                    Utils.setField(groupPurchase, "sellerId", memberIds.get(i / 2));
                    Utils.setField(groupPurchase, "productId", productIds.get(i));

                    int minQuantity = ThreadLocalRandom.current().nextInt(1, 6);
                    int maxQuantity = minQuantity + ThreadLocalRandom.current().nextInt(0, 101);
                    Utils.setField(groupPurchase, "minQuantity", minQuantity);
                    Utils.setField(groupPurchase, "maxQuantity", maxQuantity);
                    Utils.setField(groupPurchase, "currentQuantity", 0);

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

                    return groupPurchase;
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

    private void writeGroupPurchaseCsv(List<GroupPurchase> groupPurchases, Path output) throws IOException {
        Files.createDirectories(output.getParent());
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(GroupPurchaseCsvRow.class).withHeader();

        try (var writer = Files.newBufferedWriter(output);
             var sequenceWriter = mapper.writer(schema).writeValues(writer)) {
            for (GroupPurchase groupPurchase : groupPurchases) {
                sequenceWriter.write(toCsvRow(groupPurchase));
            }
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
}
