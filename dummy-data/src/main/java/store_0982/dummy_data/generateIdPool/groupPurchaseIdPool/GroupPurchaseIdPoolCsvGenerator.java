package store_0982.dummy_data.generateIdPool.groupPurchaseIdPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class GroupPurchaseIdPoolCsvGenerator {

    public static void generate(Path output, int groupPurchaseCount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("group_purchase_id");
            writer.newLine();

            for (int i = 0; i < groupPurchaseCount; i++) {
                UUID groupPurchaseId = UUID.randomUUID();
                writer.write(groupPurchaseId.toString());
                writer.newLine();
            }
        }
    }
}
