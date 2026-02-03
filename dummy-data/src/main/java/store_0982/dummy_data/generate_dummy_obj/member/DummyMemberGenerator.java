package store_0982.dummy_data.generate_dummy_obj.member;

import org.jeasy.random.EasyRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.member.domain.member.Member;
import store_0982.dummy_data.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class DummyMemberGenerator {
    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int count;
    @Value("${dummy-data.member-dummy.path}")
    private String dummyPath;
    public void readIdAndWriteMember() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);

        try (BufferedReader reader = Files.newBufferedReader(idPool)) {
            Files.createDirectories(output.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(output)) {
                String line;
                int generated = 0;

                while (generated < count && (line = reader.readLine()) != null) {
                    UUID memberId = UUID.fromString(line.trim());
                    Member dummyMember = createDummyMember(memberId);
                    writer.write(String.join(",",
                            dummyMember.getMemberId().toString(),
                            dummyMember.getRole().name(),
                            dummyMember.getCreatedAt().toString(),
                            dummyMember.getUpdatedAt().toString(),
                            dummyMember.getDeletedAt() == null ? "" : dummyMember.getDeletedAt().toString(),
                            dummyMember.getStatus().name()
                    ));
                    writer.newLine();
                    generated++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy members", e);
        }
    }

    private static Member createDummyMember(UUID memberId) {
        EasyRandom easyRandom = new EasyRandom();
        Member dummyMember = easyRandom.nextObject(Member.class);

        Utils.setField(dummyMember, "memberId", memberId);
        Utils.setField(dummyMember, "createdAt", OffsetDateTime.now());
        Utils.setField(dummyMember, "updatedAt", OffsetDateTime.now());

        return dummyMember;
    }
}
