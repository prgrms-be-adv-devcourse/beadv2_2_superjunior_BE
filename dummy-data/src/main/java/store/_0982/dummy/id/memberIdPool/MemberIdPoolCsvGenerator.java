package store._0982.dummy.id.memberIdPool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class MemberIdPoolCsvGenerator {

    public static void generate(Path output, int memberCount) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (int i = 0; i < memberCount; i++) {
                UUID memberId = UUID.randomUUID();
                writer.write(memberId.toString());
                writer.newLine();
            }
        }
    }
}
