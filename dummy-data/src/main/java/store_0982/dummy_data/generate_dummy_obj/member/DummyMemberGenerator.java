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
import store._0982.common.auth.Role;
import store._0982.member.domain.member.Member;
import store_0982.dummy_data.generate_dummy_obj.member.dto.MemberRowCsv;
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
public class DummyMemberGenerator {
    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int count;
    @Value("${dummy-data.member-dummy.path}")
    private String dummyPath;
    private static final char[] ALPHANUM = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters()
            .collectionSizeRange(0,0)
            .randomizationDepth(1)
            .randomize(FieldPredicates.named("email").and(FieldPredicates.inClass(Member.class)), randomString(100))
            .randomize(FieldPredicates.named("name").and(FieldPredicates.inClass(Member.class)), randomString(100))
            .randomize(FieldPredicates.named("password").and(FieldPredicates.inClass(Member.class)), randomString(60))
            .randomize(FieldPredicates.named("phoneNumber").and(FieldPredicates.inClass(Member.class)), randomString(20))
            .randomize(FieldPredicates.named("saltKey").and(FieldPredicates.inClass(Member.class)), randomString(32))
            .randomize(FieldPredicates.named("imageUrl").and(FieldPredicates.inClass(Member.class)), randomString(2048))
    );

    public void readIdAndWriteMember() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);
        CsvMapper csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        CsvSchema schema = csvMapper.schemaFor(MemberRowCsv.class).withHeader();
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
                int generated = 0;
                String line;
                while (generated < count && (line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    UUID memberId = UUID.fromString(trimmed);
                    Member dummyMember = createDummyMember(memberId);
                    sequenceWriter.write(MemberRowCsv.from(dummyMember));
                    generated++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy members", e);
        }
    }

    private Member createDummyMember(UUID memberId) {
        Member dummyMember = easyRandom.nextObject(Member.class);
        Utils.setField(dummyMember, "memberId", memberId);
        Utils.setField(dummyMember, "email", memberId + "@dummy.local");
        Utils.setField(dummyMember, "createdAt", OffsetDateTime.now());
        Utils.setField(dummyMember, "updatedAt", OffsetDateTime.now());
        Utils.setField(dummyMember, "role", Role.SELLER);
        Utils.setField(dummyMember, "status", Member.Status.ACTIVE);
        return dummyMember;
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
}
