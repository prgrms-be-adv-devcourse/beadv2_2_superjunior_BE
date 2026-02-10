package store_0982.dummy_data.generate_dummy_obj.commerce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.OrderCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.GroupPurchaseCsvRow;
import store_0982.dummy_data.util.Utils;

@Component
public class DummyOrderGenerator {

    private static final String[] ADDRESSES = {
            "Seoul",
            "Busan",
            "Incheon",
            "Daegu",
            "Daejeon"
    };
    private static final String[] ADDRESS_DETAILS = {
            "Apt 101",
            "Suite 202",
            "Unit 303",
            "Floor 4",
            "Room 505"
    };
    private static final String[] RECEIVER_NAMES = {
            "Kim",
            "Lee",
            "Park",
            "Choi",
            "Jung"
    };

    @Value("${dummy-data.order-id-pool.path}")
    private String orderIdPoolPath;
    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.group-purchase-dummy.path}")
    private String groupPurchaseDummyPath;
    @Value("${dummy-data.order.group-purchase-count}")
    private int groupPurchaseOrderCount;
    @Value("${dummy-data.order-dummy.path}")
    private String orderDummyPath;

    public void generateAndWriteCsv(int count) throws IOException {
        int orderCount = groupPurchaseOrderCount * 10;
        if (count != orderCount) {
            throw new IllegalStateException("Order count must be group-purchase-count * 10. expected=" + orderCount
                    + ", actual=" + count);
        }
        List<UUID> orderIds = readIds(Path.of(orderIdPoolPath), orderCount);
        if (orderIds.size() < orderCount) {
            throw new IllegalStateException("Not enough order IDs. required=" + orderCount
                    + ", actual=" + orderIds.size());
        }
        int requiredMembers = orderCount / 10;
        List<UUID> memberIds = readIds(Path.of(memberIdPoolPath), requiredMembers);
        if (memberIds.size() < requiredMembers) {
            throw new IllegalStateException("Not enough member IDs for orders. required=" + requiredMembers
                    + ", actual=" + memberIds.size());
        }

        List<GroupPurchaseCsvRow> groupPurchaseRows = readGroupPurchaseRows(
                Path.of(groupPurchaseDummyPath),
                groupPurchaseOrderCount
        );
        if (groupPurchaseRows.size() < groupPurchaseOrderCount) {
            throw new IllegalStateException("Not enough group purchase rows for orders. required="
                    + groupPurchaseOrderCount + ", actual=" + groupPurchaseRows.size());
        }

        EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters());
        Path output = Path.of(orderDummyPath);
        Files.createDirectories(output.getParent());

        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(OrderCsvRow.class).withHeader();

        try (var writer = Files.newBufferedWriter(output);
             var sequenceWriter = mapper.writer(schema).writeValues(writer)) {
            writer.write('\uFEFF');
            for (int i = 0; i < orderCount; i++) {
                Order order = easyRandom.nextObject(Order.class);
                Utils.setField(order, "orderId", orderIds.get(i));
                Utils.setField(order, "orderNumber", Order.generateOrderNumber());
                int quantity = randomQuantity();
                Utils.setField(order, "quantity", quantity);
                Utils.setField(order, "status", OrderStatus.PENDING);
                int memberIdx = i / 10;
                int slot = i % 10;
                int gpIdx = (memberIdx * 10 + slot) % groupPurchaseRows.size();
                GroupPurchaseCsvRow groupPurchaseRow = groupPurchaseRows.get(gpIdx);
                long price = groupPurchaseRow.discountedPrice() * quantity;
                Utils.setField(order, "price", price);
                Utils.setField(order, "paidPrice", price);
                int shiftedMemberIdx = (memberIdx + 1) % memberIds.size();
                Utils.setField(order, "memberId", memberIds.get(shiftedMemberIdx));
                Utils.setField(order, "address", pick(ADDRESSES));
                Utils.setField(order, "addressDetail", pick(ADDRESS_DETAILS));
                Utils.setField(order, "postalCode", randomPostalCode());
                Utils.setField(order, "receiverName", pick(RECEIVER_NAMES));
                Utils.setField(order, "sellerId", groupPurchaseRow.sellerId());
                Utils.setField(order, "groupPurchaseId", groupPurchaseRow.groupPurchaseId());
                Utils.setField(order, "idempotencyKey", UUID.randomUUID().toString());
                Utils.setField(order, "paymentMethod", randomPaymentMethod());
                OffsetDateTime createdAt = randomCreatedAt();
                OffsetDateTime updatedAt = randomUpdatedAt(createdAt);
                Utils.setField(order, "expiredAt", createdAt.plusMinutes(ThreadLocalRandom.current().nextInt(10, 121)));
                Utils.setField(order, "paidAt", null);
                Utils.setField(order, "canceledAt", null);
                Utils.setField(order, "createdAt", createdAt);
                Utils.setField(order, "updatedAt", updatedAt);
                Utils.setField(order, "deletedAt", null);

                sequenceWriter.write(toCsvRow(order));
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

    private List<GroupPurchaseCsvRow> readGroupPurchaseRows(Path path, int count) throws IOException {
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(GroupPurchaseCsvRow.class).withHeader();
        List<GroupPurchaseCsvRow> rows = new ArrayList<>(count);
        try (var reader = Files.newBufferedReader(path);
             MappingIterator<GroupPurchaseCsvRow> iterator =
                     mapper.readerFor(GroupPurchaseCsvRow.class).with(schema).readValues(reader)) {
            while (iterator.hasNext() && rows.size() < count) {
                rows.add(iterator.next());
            }
        }
        return rows;
    }

    private OrderCsvRow toCsvRow(Order order) {
        return new OrderCsvRow(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getQuantity(),
                order.getPrice(),
                order.getPaidPrice(),
                order.getStatus(),
                order.getMemberId(),
                order.getAddress(),
                order.getAddressDetail(),
                order.getPostalCode(),
                order.getReceiverName(),
                order.getSellerId(),
                order.getGroupPurchaseId(),
                order.getIdempotencyKey(),
                order.getPaymentMethod(),
                order.getExpiredAt(),
                order.getPaidAt(),
                order.getCanceledAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getDeletedAt()
        );
    }

    private int randomQuantity() {
        return ThreadLocalRandom.current().nextInt(1, 6);
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

    private PaymentMethod randomPaymentMethod() {
        return ThreadLocalRandom.current().nextBoolean() ? PaymentMethod.POINT : PaymentMethod.PG;
    }

    private String randomPostalCode() {
        int code = ThreadLocalRandom.current().nextInt(10000, 100000);
        return String.valueOf(code);
    }

    private String pick(String[] options) {
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }
}
