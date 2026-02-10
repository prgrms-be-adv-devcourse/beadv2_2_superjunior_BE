package store_0982.dummy_data.generate_dummy_obj.commerce;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.GroupPurchaseCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.OrderCsvRow;
import store_0982.dummy_data.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    private static final double CANCEL_RATE = 0.2;
    private static final double CONFIRM_RATE = 0.8;

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
        List<GroupPurchaseCsvRow> groupPurchaseRows = DummyGroupPurchaseReader.read(
                Path.of(groupPurchaseDummyPath),
                groupPurchaseOrderCount
        );
        if (groupPurchaseRows.size() < groupPurchaseOrderCount) {
            throw new IllegalStateException("Not enough group purchase rows. required=" + groupPurchaseOrderCount
                    + ", actual=" + groupPurchaseRows.size());
        }

        EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters());
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(OrderCsvRow.class).withHeader();

        Path output = Path.of(orderDummyPath);
        Files.createDirectories(output.getParent());

        try (var writer = Files.newBufferedWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {
            writer.write('\uFEFF');
            for (int i = 0; i < orderCount; i++) {
                Order order = easyRandom.nextObject(Order.class);
                Utils.setField(order, "orderId", orderIds.get(i));
                Utils.setField(order, "orderNumber", Order.generateOrderNumber());
                int quantity = randomQuantity();
                Utils.setField(order, "quantity", quantity);

                int memberIdx = i / 10;
                int slot = i % 10;
                int gpIdx = (memberIdx * 10 + slot) % groupPurchaseRows.size();
                GroupPurchaseCsvRow groupPurchaseRow = groupPurchaseRows.get(gpIdx);
                long price = groupPurchaseRow.discountedPrice() * quantity;

                Utils.setField(order, "price", price);
                Utils.setField(order, "paidPrice", price);
                Utils.setField(order, "memberId", memberIds.get(memberIdx));
                Utils.setField(order, "address", pick(ADDRESSES));
                Utils.setField(order, "addressDetail", pick(ADDRESS_DETAILS));
                Utils.setField(order, "postalCode", randomPostalCode());
                Utils.setField(order, "receiverName", pick(RECEIVER_NAMES));
                Utils.setField(order, "sellerId", groupPurchaseRow.sellerId());
                Utils.setField(order, "groupPurchaseId", groupPurchaseRow.groupPurchaseId());
                Utils.setField(order, "idempotencyKey", UUID.randomUUID().toString());

                OffsetDateTime createdAt = randomCreatedAt();
                OffsetDateTime updatedAt = randomUpdatedAt(createdAt);
                Utils.setField(order, "createdAt", createdAt);
                Utils.setField(order, "updatedAt", updatedAt);
                Utils.setField(order, "deletedAt", null);
                Utils.setField(order, "expiredAt", createdAt.plusMinutes(ThreadLocalRandom.current().nextInt(10, 121)));

                OrderStatus status = randomFinalStatus();
                Utils.setField(order, "status", status);
                if (status == OrderStatus.PENDING) {
                    Utils.setField(order, "paymentMethod", null);
                    Utils.setField(order, "paidAt", null);
                    Utils.setField(order, "canceledAt", null);
                } else {
                    PaymentMethod paymentMethod = randomPaymentMethod();
                    Utils.setField(order, "paymentMethod", paymentMethod);
                    OffsetDateTime paidAt = createdAt.plusMinutes(ThreadLocalRandom.current().nextInt(1, 60));
                    Utils.setField(order, "paidAt", paidAt);
                    if (status == OrderStatus.CANCELLED) {
                        OffsetDateTime canceledAt = paidAt.plusMinutes(ThreadLocalRandom.current().nextInt(1, 60));
                        Utils.setField(order, "canceledAt", canceledAt);
                    } else {
                        Utils.setField(order, "canceledAt", null);
                    }
                }

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

    private OrderStatus randomFinalStatus() {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < CANCEL_RATE) {
            return OrderStatus.CANCELLED;
        }
        if (roll < CANCEL_RATE + CONFIRM_RATE) {
            return OrderStatus.CONFIRMED;
        }
        return OrderStatus.PENDING;
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

    private static class DummyGroupPurchaseReader {
        static List<GroupPurchaseCsvRow> read(Path path, int count) throws IOException {
            CsvMapper mapper = new CsvMapper();
            mapper.findAndRegisterModules();
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            CsvSchema schema = mapper.schemaFor(GroupPurchaseCsvRow.class).withHeader();
            try (var reader = Files.newBufferedReader(path);
                 com.fasterxml.jackson.databind.MappingIterator<GroupPurchaseCsvRow> iterator =
                         mapper.readerFor(GroupPurchaseCsvRow.class).with(schema).readValues(reader)) {
                List<GroupPurchaseCsvRow> rows = new java.util.ArrayList<>(count);
                while (iterator.hasNext() && rows.size() < count) {
                    rows.add(iterator.next());
                }
                return rows;
            }
        }
    }
}
