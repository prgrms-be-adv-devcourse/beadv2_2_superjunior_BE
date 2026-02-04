package store_0982.dummy_data.generate_dummy_obj.member;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.member.domain.member.Seller;
import store_0982.dummy_data.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Component
public class DummySellerGenerator {
    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int count;
    @Value("${dummy-data.seller-dummy.path}")
    private String dummyPath;
    private final EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters().collectionSizeRange(0,0).randomizationDepth(1));

    public void readIdAndWriteSeller() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);
        List<String> excludedFields = new LinkedList<>();
        excludedFields.add("member"); // avoid circular reference in CSV

        try {
            Files.createDirectories(output.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create output directory", e);
        }

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = Files.newBufferedWriter(output)) {
            String headerLine = Utils.makeCsvHeaderString(Seller.class, excludedFields);
            writer.write(headerLine);

            String line;
            int generated = 0;

            while (generated < count && (line = reader.readLine()) != null) {
                UUID memberId = UUID.fromString(line.trim());
                Seller dummySeller = createDummySeller(memberId);
                String row = Utils.makeCsvRowString(dummySeller, excludedFields);
                writer.write(row);
                generated++;
            }
            writer.flush();
        } catch (IOException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to generate dummy sellers", e);
        }
    }

    private Seller createDummySeller(UUID memberId) {
        Seller dummySeller = easyRandom.nextObject(Seller.class);
        Utils.setField(dummySeller, "sellerId", memberId);
        Utils.setField(dummySeller, "createdAt", OffsetDateTime.now());
        Utils.setField(dummySeller, "updatedAt", OffsetDateTime.now());
        dummySeller.confirm();

        return dummySeller;
    }
}
