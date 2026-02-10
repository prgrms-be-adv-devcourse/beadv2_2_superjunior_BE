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

        CsvMapper mapper = baseMapper();
        CsvSchema orderSchema = mapper.schemaFor(OrderCsvRow.class).withHeader();
        CsvSchema settlementSchema = mapper.schemaFor(OrderSettlementCsvRow.class).withHeader();

        Path output = Path.of(orderSettlementDummyPath);
        Files.createDirectories(output.getParent());

        try (var orderReader = Files.newBufferedReader(orderInput);
             MappingIterator<OrderCsvRow> orderIterator =
                     mapper.readerFor(OrderCsvRow.class).with(orderSchema).readValues(orderReader);
             var settlementWriter = Files.newBufferedWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(settlementSchema).writeValues(settlementWriter)) {

            settlementWriter.write('\uFEFF');

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
