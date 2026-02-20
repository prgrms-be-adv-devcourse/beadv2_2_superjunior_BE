package store._0982.dummy.object.commerce;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.OrderStatus;
import store._0982.dummy.object.commerce.row.CanceledOrderCsvRow;
import store._0982.dummy.object.commerce.row.OrderCsvRow;
import store._0982.dummy.util.CsvWriterUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummyCanceledOrderGenerator {

    @Value("${dummy-data.order-dummy.path}")
    private String orderDummyPath;

    @Value("${dummy-data.canceled-order-dummy.path}")
    private String canceledOrderDummyPath;

    public void generate() throws IOException {
        Path orderInput = Path.of(orderDummyPath);
        if (!Files.exists(orderInput)) {
            throw new IllegalStateException("Order dummy file not found: " + orderInput);
        }

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema orderSchema = CsvWriterUtil.schemaFor(mapper, OrderCsvRow.class);
        CsvSchema canceledSchema = CsvWriterUtil.schemaFor(mapper, CanceledOrderCsvRow.class);

        Path output = Path.of(canceledOrderDummyPath);

        try (var orderReader = Files.newBufferedReader(orderInput);
             MappingIterator<OrderCsvRow> orderIterator =
                     mapper.readerFor(OrderCsvRow.class).with(orderSchema).readValues(orderReader);
             var canceledWriter = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(canceledSchema).writeValues(canceledWriter)) {

            while (orderIterator.hasNext()) {
                OrderCsvRow order = orderIterator.next();
                if (order.status() != OrderStatus.CANCELLED) {
                    continue;
                }
                UUID canceledOrderId = UUID.randomUUID();
                CancelReason reason = randomCancelReason();
                CancelStatus status = randomStatus(reason);
                CanceledOrderCsvRow row = toCanceledOrderRow(canceledOrderId, order, reason, status);
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
