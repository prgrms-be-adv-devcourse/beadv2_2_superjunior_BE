package store._0982.dummy.id.orderIdPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class OrderIdPoolCsvGenerator {

    public static void generate(Path output, int orderCount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (int i = 0; i < orderCount; i++) {
                UUID orderId = UUID.randomUUID();
                writer.write(orderId.toString());
                writer.newLine();
            }
        }
    }
}
