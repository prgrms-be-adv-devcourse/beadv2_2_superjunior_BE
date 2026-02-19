package store_0982.dummy_data.generate_dummy_obj.commerce;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.domain.sellerbalance.SellerBalanceHistoryStatus;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.OrderSettlementCsvRow;
import store_0982.dummy_data.generate_dummy_obj.commerce.row.SellerBalanceHistoryCsvRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummySellerBalanceHistoryGenerator {

    @Value("${dummy-data.order-settlement-dummy.path}")
    private String orderSettlementDummyPath;

    @Value("${dummy-data.seller-balance-history-dummy.path}")
    private String sellerBalanceHistoryDummyPath;

    public void generate() throws IOException {
        Path settlementInput = Path.of(orderSettlementDummyPath);
        if (!Files.exists(settlementInput)) {
            throw new IllegalStateException("Order settlement dummy file not found: " + settlementInput);
        }

        CsvMapper mapper = baseMapper();
        CsvSchema settlementSchema = mapper.schemaFor(OrderSettlementCsvRow.class).withHeader();
        CsvSchema historySchema = mapper.schemaFor(SellerBalanceHistoryCsvRow.class).withHeader();

        Path output = Path.of(sellerBalanceHistoryDummyPath);
        Files.createDirectories(output.getParent());
        Map<UUID, SellerBalanceState> sellerStates = new HashMap<>();

        try (var settlementReader = Files.newBufferedReader(settlementInput);
             MappingIterator<OrderSettlementCsvRow> settlementIterator =
                     mapper.readerFor(OrderSettlementCsvRow.class).with(settlementSchema).readValues(settlementReader);
             var historyWriter = Files.newBufferedWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(historySchema).writeValues(historyWriter)) {

            historyWriter.write('\uFEFF');

            while (settlementIterator.hasNext()) {
                OrderSettlementCsvRow settlement = settlementIterator.next();
                SellerBalanceHistoryCsvRow creditHistory = createCreditHistory(settlement);
                sequenceWriter.write(creditHistory);

                long creditAmount = creditHistory.amount() != null ? creditHistory.amount() : 0L;
                UUID sellerId = settlement.sellerId();
                SellerBalanceState state = sellerStates.computeIfAbsent(
                        sellerId, key -> SellerBalanceState.initialState(creditHistory.createdAt())
                );
                state.addCredit(creditAmount, creditHistory.createdAt());
                emitDebitsIfNeeded(sellerId, state, sequenceWriter);
            }

            for (Map.Entry<UUID, SellerBalanceState> entry : sellerStates.entrySet()) {
                SellerBalanceState state = entry.getValue();
                if (state.hasRemainingBalance()) {
                    SellerBalanceHistoryCsvRow finalDebit = createDebitHistory(
                            entry.getKey(),
                            state.consumeAll(),
                            state.nextEventTime()
                    );
                    sequenceWriter.write(finalDebit);
                }
            }
        }
    }

    private SellerBalanceHistoryCsvRow createCreditHistory(OrderSettlementCsvRow settlement) {
        UUID historyId = UUID.randomUUID();
        UUID memberId = settlement.sellerId();
        UUID orderSettlementId = settlement.orderSettlementId();
        long amount = settlement.settlementAmount() != null ? settlement.settlementAmount() : 0L;
        OffsetDateTime createdAt = settlement.createdAt() != null ? settlement.createdAt() : OffsetDateTime.now();

        return new SellerBalanceHistoryCsvRow(
                historyId,
                memberId,
                null,
                orderSettlementId,
                amount,
                SellerBalanceHistoryStatus.CREDIT,
                createdAt
        );
    }

    private void emitDebitsIfNeeded(UUID sellerId,
                                    SellerBalanceState state,
                                    SequenceWriter sequenceWriter) throws IOException {
        while (state.shouldTriggerPayout()) {
            long payoutAmount = state.createPayoutAmount();
            OffsetDateTime payoutAt = state.nextEventTime();
            SellerBalanceHistoryCsvRow debitHistory = createDebitHistory(sellerId, payoutAmount, payoutAt);
            sequenceWriter.write(debitHistory);
        }
    }

    private SellerBalanceHistoryCsvRow createDebitHistory(UUID sellerId,
                                                          long payoutAmount,
                                                          OffsetDateTime payoutAt) {
        return new SellerBalanceHistoryCsvRow(
                UUID.randomUUID(),
                sellerId,
                UUID.randomUUID(),
                null,
                payoutAmount,
                SellerBalanceHistoryStatus.DEBIT,
                payoutAt
        );
    }

    private CsvMapper baseMapper() {
        CsvMapper mapper = new CsvMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    private static final long MIN_PAYOUT_THRESHOLD = 200_000L;
    private static final long MAX_PAYOUT_THRESHOLD = 800_000L;
    private static final double MIN_PAYOUT_RATIO = 0.4;
    private static final double MAX_PAYOUT_RATIO = 0.85;

    private static long randomThreshold() {
        long range = MAX_PAYOUT_THRESHOLD - MIN_PAYOUT_THRESHOLD;
        return MIN_PAYOUT_THRESHOLD + ThreadLocalRandom.current().nextLong(range + 1);
    }

    private static double randomRatio() {
        double range = MAX_PAYOUT_RATIO - MIN_PAYOUT_RATIO;
        return MIN_PAYOUT_RATIO + ThreadLocalRandom.current().nextDouble(range);
    }

    private static final class SellerBalanceState {
        private long balance;
        private long nextThreshold;
        private OffsetDateTime lastEventTime;

        private SellerBalanceState(long nextThreshold, OffsetDateTime lastEventTime) {
            this.nextThreshold = nextThreshold;
            this.lastEventTime = lastEventTime;
        }

        private static SellerBalanceState initialState(OffsetDateTime baseTime) {
            return new SellerBalanceState(randomThreshold(), baseTime);
        }

        private void addCredit(long amount, OffsetDateTime eventTime) {
            this.balance += amount;
            this.lastEventTime = eventTime;
        }

        private boolean shouldTriggerPayout() {
            return balance >= nextThreshold && balance > 0;
        }

        private long createPayoutAmount() {
            double ratio = randomRatio();
            long payout = (long) Math.max(1L, Math.round(balance * ratio));
            payout = Math.min(payout, balance);
            this.balance -= payout;
            this.nextThreshold = randomThreshold();
            return payout;
        }

        OffsetDateTime nextEventTime() {
            long hours = ThreadLocalRandom.current().nextLong(6, 73);
            OffsetDateTime base = lastEventTime != null ? lastEventTime : OffsetDateTime.now();
            OffsetDateTime eventTime = base.plusHours(hours);
            this.lastEventTime = eventTime;
            return eventTime;
        }

        private boolean hasRemainingBalance() {
            return balance > 0;
        }

        private long consumeAll() {
            long payout = balance;
            balance = 0;
            nextThreshold = randomThreshold();
            return payout;
        }
    }
}
