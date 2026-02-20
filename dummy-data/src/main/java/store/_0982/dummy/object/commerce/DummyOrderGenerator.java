package store._0982.dummy.object.commerce;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.dummy.constants.DummyDataConstants;
import store._0982.dummy.object.commerce.row.GroupPurchaseCsvRow;
import store._0982.dummy.object.commerce.row.OrderCsvRow;
import store._0982.dummy.util.CsvWriterUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DummyOrderGenerator {

    private static final String[] ADDRESSES = {"Seoul", "Busan", "Incheon", "Daegu", "Daejeon"};
    private static final String[] ADDRESS_DETAILS = {"Apt 101", "Suite 202", "Unit 303", "Floor 4", "Room 505"};
    private static final String[] RECEIVER_NAMES = {"Kim", "Lee", "Park", "Choi", "Jung"};

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
        List<UUID> memberIds = readIds(Path.of(memberIdPoolPath), orderCount / 10);
        List<GroupPurchaseCsvRow> groupPurchaseRows = readGroupPurchases(
                Path.of(groupPurchaseDummyPath), groupPurchaseOrderCount);
        if (groupPurchaseRows.size() < groupPurchaseOrderCount) {
            throw new IllegalStateException("Not enough group purchase rows. required=" + groupPurchaseOrderCount
                    + ", actual=" + groupPurchaseRows.size());
        }

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, OrderCsvRow.class);
        Path output = Path.of(orderDummyPath);

        try (var writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            for (int i = 0; i < orderCount; i++) {
                int memberIdx = i / 10;
                int gpIdx = (memberIdx * 10 + (i % 10)) % groupPurchaseRows.size();
                GroupPurchaseCsvRow gp = groupPurchaseRows.get(gpIdx);

                int quantity = ThreadLocalRandom.current().nextInt(
                        DummyDataConstants.ORDER_MIN_QUANTITY, DummyDataConstants.ORDER_MAX_QUANTITY + 1);
                long price = gp.discountedPrice() * quantity;
                OffsetDateTime createdAt = randomCreatedAt();
                OrderStatus status = randomFinalStatus();

                OffsetDateTime paidAt = null;
                OffsetDateTime canceledAt = null;
                PaymentMethod paymentMethod = null;

                if (status != OrderStatus.PENDING) {
                    paymentMethod = ThreadLocalRandom.current().nextBoolean() ? PaymentMethod.POINT : PaymentMethod.PG;
                    paidAt = createdAt.plusMinutes(ThreadLocalRandom.current().nextInt(1, 60));
                    if (status == OrderStatus.CANCELLED) {
                        canceledAt = paidAt.plusMinutes(ThreadLocalRandom.current().nextInt(1, 60));
                    }
                }

                sequenceWriter.write(new OrderCsvRow(
                        orderIds.get(i),
                        Order.generateOrderNumber(),
                        quantity,
                        price,
                        price,
                        status,
                        memberIds.get(memberIdx),
                        pick(ADDRESSES),
                        pick(ADDRESS_DETAILS),
                        String.valueOf(ThreadLocalRandom.current().nextInt(10000, 100000)),
                        pick(RECEIVER_NAMES),
                        gp.sellerId(),
                        gp.groupPurchaseId(),
                        UUID.randomUUID().toString(),
                        paymentMethod,
                        createdAt.plusMinutes(ThreadLocalRandom.current().nextInt(10, 121)),
                        paidAt,
                        canceledAt,
                        createdAt,
                        randomUpdatedAt(createdAt),
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

    private List<GroupPurchaseCsvRow> readGroupPurchases(Path path, int count) throws IOException {
        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, GroupPurchaseCsvRow.class);
        try (var reader = Files.newBufferedReader(path);
             var iterator = mapper.readerFor(GroupPurchaseCsvRow.class).with(schema).<GroupPurchaseCsvRow>readValues(reader)) {
            List<GroupPurchaseCsvRow> rows = new ArrayList<>(count);
            while (iterator.hasNext() && rows.size() < count) {
                rows.add(iterator.next());
            }
            return rows;
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

    private OrderStatus randomFinalStatus() {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < DummyDataConstants.ORDER_CANCEL_RATIO) return OrderStatus.CANCELLED;
        if (roll < DummyDataConstants.ORDER_CANCEL_RATIO + DummyDataConstants.ORDER_CONFIRM_RATIO) return OrderStatus.CONFIRMED;
        return OrderStatus.PENDING;
    }

    private String pick(String[] options) {
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }
}
