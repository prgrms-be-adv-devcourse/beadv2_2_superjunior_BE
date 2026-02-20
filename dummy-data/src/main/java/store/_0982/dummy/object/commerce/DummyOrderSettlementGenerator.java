package store._0982.dummy.object.commerce;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.settlement.OrderSettlementStatus;
import store._0982.dummy.object.commerce.row.CanceledOrderCsvRow;
import store._0982.dummy.object.commerce.row.OrderCsvRow;
import store._0982.dummy.object.commerce.row.OrderSettlementCsvRow;
import store._0982.dummy.util.CsvWriterUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class DummyOrderSettlementGenerator {

    @Value("${dummy-data.order-dummy.path}")
    private String orderDummyPath;


    @Value("${dummy-data.canceled-order-dummy.path}")
    private String canceledOrderDummyPath;

    @Value("${dummy-data.order-settlement-dummy.path}")
    private String orderSettlementDummyPath;

    public void generate() throws IOException {
        Path orderInput = Path.of(orderDummyPath);
        if (!Files.exists(orderInput)) {
            throw new IllegalStateException("Order dummy file not found: " + orderInput);
        }

        Map<UUID, CanceledOrderCsvRow> canceledOrderMap = readCanceledRows();

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema orderSchema = CsvWriterUtil.schemaFor(mapper, OrderCsvRow.class);
        CsvSchema settlementSchema = CsvWriterUtil.schemaFor(mapper, OrderSettlementCsvRow.class);

        Path output = Path.of(orderSettlementDummyPath);

        try (var orderReader = Files.newBufferedReader(orderInput);
             MappingIterator<OrderCsvRow> orderIterator =
                     mapper.readerFor(OrderCsvRow.class).with(orderSchema).readValues(orderReader);
             var settlementWriter = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(settlementSchema).writeValues(settlementWriter)) {

            while (orderIterator.hasNext()) {
                OrderCsvRow order = orderIterator.next();

                if (order.status() == OrderStatus.CONFIRMED) {
                    UUID settlementId = UUID.randomUUID();
                    OrderSettlementCsvRow row = createConfirmedSettlement(order, settlementId);
                    sequenceWriter.write(row);
                    continue;
                }

                if (order.status() == OrderStatus.CANCELLED) {
                    CanceledOrderCsvRow canceledRow = canceledOrderMap.get(order.orderId());
                    if (canceledRow == null || canceledRow.status() != CancelStatus.COMPLETED) {
                        continue;
                    }
                    if (canceledRow.reason().isSellerFault()) {
                        continue;
                    }
                    UUID settlementId = UUID.randomUUID();
                    OrderSettlementCsvRow row = createCanceledSettlement(order, canceledRow, settlementId);
                    sequenceWriter.write(row);
                }
            }
        }
    }

    private Map<UUID, CanceledOrderCsvRow> readCanceledRows() throws IOException {
        Path input = Path.of(canceledOrderDummyPath);
        if (!Files.exists(input)) {
            return Map.of();
        }
        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, CanceledOrderCsvRow.class);
        try (var reader = Files.newBufferedReader(input);
             MappingIterator<CanceledOrderCsvRow> iterator =
                     mapper.readerFor(CanceledOrderCsvRow.class).with(schema).readValues(reader)) {
            Map<UUID, CanceledOrderCsvRow> rows = new HashMap<>();
            iterator.forEachRemaining(row -> rows.put(row.orderId(), row));
            return rows;
        }
    }

    private OrderSettlementCsvRow createConfirmedSettlement(OrderCsvRow order, UUID settlementId) {
        long orderAmount = order.paidPrice() != null ? order.paidPrice() : 0L;
        double feeRate = 0.2;
        long platformFee = Math.round(orderAmount * feeRate);
        long settlementAmount = orderAmount - platformFee;
        OffsetDateTime createdAt = order.createdAt() != null ? order.createdAt() : OffsetDateTime.now();
        OffsetDateTime settledAt = null;

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
        long settlementAmount = canceled.cancelFeeAmount();
        OffsetDateTime createdAt = canceled.createdAt() != null ? canceled.createdAt() : OffsetDateTime.now();

        return new OrderSettlementCsvRow(
                settlementId,
                order.sellerId(),
                order.groupPurchaseId(),
                order.orderId(),
                OrderSettlementStatus.BUYER_CANCEL,
                0L,
                0.0,
                0L,
                settlementAmount,
                createdAt,
                null
        );
    }

}
