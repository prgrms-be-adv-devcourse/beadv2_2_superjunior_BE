package store_0982.dummy_data.generate_dummy_obj.commerce;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.OrderStatus;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.CanceledOrderCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.OrderCsvRow;

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
public class DummyCanceledOrderGenerator {

    @Value("${dummy-data.order-dummy.path}")
    private String orderDummyPath;

    @Value("${dummy-data.canceled-order-dummy.path}")
    private String canceledOrderDummyPath;

    @Value("${dummy-data.canceled-order-id-pool.path}")
    private String canceledOrderIdPoolPath;

    public void generate() throws IOException {
        List<OrderCsvRow> orders = readOrderRows();
        List<UUID> canceledOrderIds = readIds(Path.of(canceledOrderIdPoolPath));
        List<CanceledOrderCsvRow> rows = new ArrayList<>();
        int canceledIndex = 0;

        for (OrderCsvRow order : orders) {
            if (order.status() != OrderStatus.CANCELLED) {
                continue;
            }
            if (canceledIndex >= canceledOrderIds.size()) {
                throw new IllegalStateException("Not enough canceled order IDs. required >= "
                        + (rows.size() + 1) + ", actual=" + canceledOrderIds.size());
            }
            UUID canceledOrderId = canceledOrderIds.get(canceledIndex++);
            CancelReason reason = randomCancelReason();
            CancelStatus status = randomStatus(reason);
            rows.add(toCanceledOrderRow(canceledOrderId, order, reason, status));
        }

        writeRows(rows);
    }

    private List<OrderCsvRow> readOrderRows() throws IOException {
        Path input = Path.of(orderDummyPath);
        if (!Files.exists(input)) {
            return List.of();
        }
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(OrderCsvRow.class).withHeader();

        try (var reader = Files.newBufferedReader(input);
             MappingIterator<OrderCsvRow> iterator =
                     mapper.readerFor(OrderCsvRow.class).with(schema).readValues(reader)) {
            List<OrderCsvRow> rows = new ArrayList<>();
            iterator.forEachRemaining(rows::add);
            return rows;
        }
    }

    private List<UUID> readIds(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IllegalStateException("Canceled order ID pool file not found: " + path);
        }
        try (var lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        }
    }

    private void writeRows(List<CanceledOrderCsvRow> rows) throws IOException {
        Path output = Path.of(canceledOrderDummyPath);
        Files.createDirectories(output.getParent());

        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        CsvSchema schema = mapper.schemaFor(CanceledOrderCsvRow.class).withHeader();

        try (var writer = Files.newBufferedWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {
            writer.write('\uFEFF');
            for (CanceledOrderCsvRow row : rows) {
                sequenceWriter.write(row);
            }
        }
    }

    private CancelReason randomCancelReason() {
        CancelReason[] reasons = CancelReason.values();
        return reasons[ThreadLocalRandom.current().nextInt(reasons.length)];
    }

    private CancelStatus randomStatus(CancelReason reason) {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (reason.isSellerFault()) {
            if (roll < 0.5) {
                return CancelStatus.COMPLETED;
            }
            if (roll < 0.7) {
                return CancelStatus.APPROVED;
            }
            if (roll < 0.85) {
                return CancelStatus.REJECTED;
            }
            return CancelStatus.PENDING;
        }
        if (roll < 0.5) {
            return CancelStatus.COMPLETED;
        }
        if (roll < 0.8) {
            return CancelStatus.REJECTED;
        }
        return CancelStatus.REQUESTED;
    }

    private CanceledOrderCsvRow toCanceledOrderRow(UUID canceledOrderId,
                                                   OrderCsvRow order,
                                                   CancelReason reason,
                                                   CancelStatus status) {
        long originalPaid = order.paidPrice() != null ? order.paidPrice() : 0L;
        long cancelFee = reason.isBuyerFault()
                ? Math.max(Math.round(originalPaid * 0.2), 0L)
                : 0L;
        long shippingFee = reason.isSellerFault() ? 6_000L : 0L;
        long refundAmount = Math.max(originalPaid - cancelFee - shippingFee, 0L);
        String policyId = reason.isBuyerFault() ? "CANCEL_POLICY_VOID_V1" : "CANCEL_POLICY_SELLER_V1";
        String policySnapshot = String.format(
                "{\"refundAmount\":%d,\"cancellationFee\":%d,\"shippingFee\":%d}",
                refundAmount,
                cancelFee,
                shippingFee
        );
        OffsetDateTime canceledAt = order.canceledAt() != null ? order.canceledAt() : OffsetDateTime.now();
        OffsetDateTime createdAt = canceledAt;
        OffsetDateTime returnedAt = null;
        OffsetDateTime updatedAt;
        if (status == CancelStatus.COMPLETED) {
            returnedAt = createdAt.plusMinutes(ThreadLocalRandom.current().nextInt(30, 240));
            updatedAt = returnedAt.plusSeconds(ThreadLocalRandom.current().nextInt(30, 600));
        } else {
            updatedAt = createdAt.plusSeconds(ThreadLocalRandom.current().nextInt(60, 600));
        }

        return new CanceledOrderCsvRow(
                canceledOrderId,
                order.orderId(),
                order.memberId(),
                order.sellerId(),
                originalPaid,
                cancelFee,
                shippingFee,
                refundAmount,
                policyId,
                policySnapshot,
                status,
                reason,
                reason.name(),
                UUID.randomUUID().toString(),
                order.paymentMethod(),
                canceledAt,
                returnedAt,
                createdAt,
                updatedAt
        );
    }
}
