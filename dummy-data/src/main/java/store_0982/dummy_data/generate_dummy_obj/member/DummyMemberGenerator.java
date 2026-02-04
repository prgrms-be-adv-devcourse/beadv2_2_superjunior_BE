package store_0982.dummy_data.generate_dummy_obj.member;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.auth.Role;
import store._0982.member.domain.member.Member;
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
public class DummyMemberGenerator {
    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int count;
    @Value("${dummy-data.member-dummy.path}")
    private String dummyPath;
    private final EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters().collectionSizeRange(0,0).randomizationDepth(1));

    public void readIdAndWriteMember() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);
        List<String> excluded = new LinkedList<>();
        excluded.add("addresses"); // avoid writing collection column
        try {
            Files.createDirectories(output.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create output directory", e);
        }

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = Files.newBufferedWriter(output)) {
            int generated = 0;
            String headerLine = Utils.makeCsvHeaderString(Member.class, excluded);
            writer.write(headerLine);

            String line;
            while (generated < count && (line = reader.readLine()) != null) {
                UUID memberId = UUID.fromString(line.trim());
                Member dummyMember = createDummyMember(memberId);
                String row = Utils.makeCsvRowString(dummyMember, excluded);
                writer.write(row);
                generated++;
            }
            writer.flush();
        } catch (IOException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to generate dummy members", e);
        }
    }

    private Member createDummyMember(UUID memberId) {
        Member dummyMember = easyRandom.nextObject(Member.class);
        Utils.setField(dummyMember, "memberId", memberId);
        Utils.setField(dummyMember, "createdAt", OffsetDateTime.now());
        Utils.setField(dummyMember, "updatedAt", OffsetDateTime.now());
        Utils.setField(dummyMember, "role", Role.SELLER);
        Utils.setField(dummyMember, "status", Member.Status.ACTIVE);
        return dummyMember;
    }
}
