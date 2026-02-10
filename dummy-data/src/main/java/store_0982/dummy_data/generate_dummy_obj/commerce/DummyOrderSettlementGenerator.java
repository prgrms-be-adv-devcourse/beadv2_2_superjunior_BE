package store_0982.dummy_data.generate_dummy_obj.commerce;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.settlement.OrderSettlementStatus;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.CanceledOrderCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.OrderCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.OrderSettlementCsvRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummyOrderSettlementGenerator {

    @Value("${dummy-data.order-dummy.path}")
    private String orderDummyPath;


    @Value("${dummy-data.canceled-order-dummy.path}")
    private String canceledOrderDummyPath;

    @Value("${dummy-data.order-settlement-dummy.path}")
    private String orderSettlementDummyPath;

    @Value("${dummy-data.order-settlement-id-pool.path}")
    private String orderSettlementIdPoolPath;

    public void generate() throws IOException {
        List<OrderCsvRow> orders = readOrderRows();
        Map<UUID, CanceledOrderCsvRow> canceledOrderMap = readCanceledRows();
        List<UUID> settlementIds = readIds(Path.of(orderSettlementIdPoolPath));
        int settlementIndex = 0;

        List<OrderSettlementCsvRow> settlements = new ArrayList<>();
        for (OrderCsvRow order : orders) {
            if (order.status() == OrderStatus.CONFIRMED) {
                settlements.add(createConfirmedSettlement(order, settlementIds.get(settlementIndex++)));
                continue;
            }
        if (order.status() == OrderStatus.CANCELLED) {
            CanceledOrderCsvRow canceledRow = canceledOrderMap.get(order.orderId());
            if (canceledRow == null || canceledRow.status() != CancelStatus.COMPLETED) {
                continue;
            }
            settlements.add(createCanceledSettlement(order, canceledRow, settlementIds.get(settlementIndex++)));
        }
        }

        writeSettlements(settlements);
    }

    private Map<UUID, CanceledOrderCsvRow> readCanceledRows() throws IOException {
        Path input = Path.of(canceledOrderDummyPath);
        if (!Files.exists(input)) {
            return Map.of();
        }
        CsvMapper mapper = baseMapper();
        CsvSchema schema = mapper.schemaFor(CanceledOrderCsvRow.class).withHeader();
        try (var reader = Files.newBufferedReader(input);
             MappingIterator<CanceledOrderCsvRow> iterator =
                     mapper.readerFor(CanceledOrderCsvRow.class).with(schema).readValues(reader)) {
            Map<UUID, CanceledOrderCsvRow> rows = new HashMap<>();
            iterator.forEachRemaining(row -> rows.put(row.orderId(), row));
            return rows;
        }
    }

    private List<OrderCsvRow> readOrderRows() throws IOException {
        Path input = Path.of(orderDummyPath);
        if (!Files.exists(input)) {
            return List.of();
        }
        CsvMapper mapper = baseMapper();
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
            return List.of();
        }
        try (var lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(UUID::fromString)
                    .toList();
        }
    }

    private void writeSettlements(List<OrderSettlementCsvRow> rows) throws IOException {
        Path output = Path.of(orderSettlementDummyPath);
        Files.createDirectories(output.getParent());
        CsvMapper mapper = baseMapper();
        CsvSchema schema = mapper.schemaFor(OrderSettlementCsvRow.class).withHeader();

        try (var writer = Files.newBufferedWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {
            writer.write('\uFEFF');
            for (OrderSettlementCsvRow row : rows) {
                sequenceWriter.write(row);
            }
        }
    }

    private OrderSettlementCsvRow createConfirmedSettlement(OrderCsvRow order, UUID settlementId) {
        long orderAmount = order.paidPrice() != null ? order.paidPrice() : 0L;
        double feeRate = 0.2;
        long platformFee = Math.round(orderAmount * feeRate);
        long settlementAmount = orderAmount - platformFee;
        OffsetDateTime createdAt = order.createdAt() != null ? order.createdAt() : OffsetDateTime.now();
        OffsetDateTime settledAt = createdAt.plusDays(ThreadLocalRandom.current().nextInt(1, 8));

        return new OrderSettlementCsvRow(
                settlementId,
                order.sellerId(),
                order.groupPurchaseId(),
                order.orderId(),
                OrderSettlementStatus.COMPLETED,
                orderAmount,
                feeRate,
                platformFee,
                settlementAmount,
                createdAt,
                settledAt
        );
    }

    private OrderSettlementCsvRow createCanceledSettlement(OrderCsvRow order,
                                                           CanceledOrderCsvRow canceled,
                                                           UUID settlementId) {
        boolean buyerFault = canceled.reason().isBuyerFault();
        OrderSettlementStatus status = buyerFault
                ? OrderSettlementStatus.BUYER_CANCEL
                : OrderSettlementStatus.SELLER_CANCEL;
        long settlementAmount = buyerFault
                ? canceled.cancelFeeAmount()
                : -canceled.shippingFeeAmount();
        OffsetDateTime createdAt = canceled.createdAt() != null ? canceled.createdAt() : OffsetDateTime.now();

        return new OrderSettlementCsvRow(
                settlementId,
                order.sellerId(),
                order.groupPurchaseId(),
                order.orderId(),
                status,
                0L,
                0.0,
                0L,
                settlementAmount,
                createdAt,
                null
        );
    }

    private CsvMapper baseMapper() {
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
