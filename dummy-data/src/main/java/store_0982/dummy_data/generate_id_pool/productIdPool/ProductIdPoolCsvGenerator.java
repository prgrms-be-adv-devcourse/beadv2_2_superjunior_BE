package store_0982.dummy_data.generate_id_pool.productIdPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ProductIdPoolCsvGenerator {

    public static void generate(Path output, int productCount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (int i = 0; i < productCount; i++) {
                UUID productId = UUID.randomUUID();
                writer.write(productId.toString());
                writer.newLine();
            }
        }
    }
}
