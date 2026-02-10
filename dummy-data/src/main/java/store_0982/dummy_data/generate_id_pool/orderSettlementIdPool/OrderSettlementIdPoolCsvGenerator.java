package store_0982.dummy_data.generate_id_pool.orderSettlementIdPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class OrderSettlementIdPoolCsvGenerator {

    public static void generate(Path output, int orderSettlementCount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (int i = 0; i < orderSettlementCount; i++) {
                UUID orderSettlementId = UUID.randomUUID();
                writer.write(orderSettlementId.toString());
                writer.newLine();
            }
        }
    }
}
