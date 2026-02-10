package store_0982.dummy_data.generate_dummy_obj.member;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.jeasy.random.api.Randomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.member.domain.member.Seller;
import store_0982.dummy_data.generate_dummy_obj.member.dto.SellerRowCsv;
import store_0982.dummy_data.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummySellerGenerator {
    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int count;
    @Value("${dummy-data.seller-dummy.path}")
    private String dummyPath;
    private static final char[] ALPHANUM = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters()
            .collectionSizeRange(0,0)
            .randomizationDepth(1)
            .randomize(FieldPredicates.named("bankCode").and(FieldPredicates.inClass(Seller.class)), randomString(20))
            .randomize(FieldPredicates.named("accountNumber").and(FieldPredicates.inClass(Seller.class)), randomString(20))
            .randomize(FieldPredicates.named("accountHolder").and(FieldPredicates.inClass(Seller.class)), randomString(50))
    );

    public void readIdAndWriteSeller() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);
        CsvMapper csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        CsvSchema schema = csvMapper.schemaFor(SellerRowCsv.class).withHeader();
        //Timstamp 에러 해결
        csvMapper.findAndRegisterModules();
        csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            Files.createDirectories(output.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create output directory", e);
        }

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write('\uFEFF');
            try (SequenceWriter sequenceWriter = csvMapper.writer(schema).writeValues(writer)) {
                String line;
                int generated = 0;

                while (generated < count && (line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    UUID memberId = UUID.fromString(trimmed);
                    Seller dummySeller = createDummySeller(memberId);
                    sequenceWriter.write(SellerRowCsv.from(dummySeller));
                    generated++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy sellers", e);
        }
    }

    private Seller createDummySeller(UUID memberId) {
        Seller dummySeller = easyRandom.nextObject(Seller.class);
        Utils.setField(dummySeller, "sellerId", memberId);
        Utils.setField(dummySeller, "businessRegistrationNumber", toBusinessRegistrationNumber(memberId));
        Utils.setField(dummySeller, "createdAt", OffsetDateTime.now());
        Utils.setField(dummySeller, "updatedAt", OffsetDateTime.now());
        dummySeller.confirm();

        return dummySeller;
    }

    private static Randomizer<String> randomString(int maxLength) {
        return () -> {
            int length = ThreadLocalRandom.current().nextInt(1, maxLength + 1);
            char[] buffer = new char[length];
            for (int i = 0; i < length; i++) {
                buffer[i] = ALPHANUM[ThreadLocalRandom.current().nextInt(ALPHANUM.length)];
            }
            return new String(buffer);
        };
    }

    private static String toBusinessRegistrationNumber(UUID memberId) {
        long value = memberId.getMostSignificantBits() ^ memberId.getLeastSignificantBits();
        String encoded = Long.toUnsignedString(value, 36).toUpperCase();
        if (encoded.length() >= 15) {
            return encoded.substring(encoded.length() - 15);
        }
        return "0".repeat(15 - encoded.length()) +
                encoded;
    }
}
