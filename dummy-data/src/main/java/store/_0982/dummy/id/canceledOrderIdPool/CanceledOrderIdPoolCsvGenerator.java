package store._0982.dummy.id.canceledOrderIdPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class CanceledOrderIdPoolCsvGenerator {

    public static void generate(Path output, int count) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (int i = 0; i < count; i++) {
                writer.write(UUID.randomUUID().toString());
                writer.newLine();
            }
        }
    }
}
