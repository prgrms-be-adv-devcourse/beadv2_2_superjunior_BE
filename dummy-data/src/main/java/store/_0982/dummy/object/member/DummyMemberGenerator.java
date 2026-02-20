package store._0982.dummy.object.member;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import store._0982.common.auth.Role;
import store._0982.member.domain.member.Member;
import store._0982.dummy.constants.DummyDataConstants;
import store._0982.dummy.object.member.dto.MemberRowCsv;
import store._0982.dummy.util.CsvWriterUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@SuppressWarnings("java:S6437")
public class DummyMemberGenerator {

    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int count;
    @Value("${dummy-data.member-dummy.path}")
    private String dummyPath;

    // PasswordEncoderConfig의 BCryptPasswordEncoder(11)과 동일한 strength 사용
    // 클래스 로딩 시 1회만 계산해 100,000번 반복 연산 회피
    private static final String ENCODED_PASSWORD = new BCryptPasswordEncoder(11)
            .encode(DummyDataConstants.DUMMY_SALT_KEY + DummyDataConstants.DUMMY_PLAIN_PASSWORD);

    private static final String[] DUMMY_NAMES = {
            "김더미", "이테스트", "박샘플", "최더미", "정테스트",
            "강샘플", "조더미", "윤테스트", "장샘플", "임더미"
    };
    private static final String[] PHONE_PREFIXES = {"010", "011", "016", "017", "019"};

    public void readIdAndWriteMember() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, MemberRowCsv.class);

        int sellerCount = (int) (count * DummyDataConstants.SELLER_RATIO);

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            int generated = 0;
            String line;
            while (generated < count && (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                UUID memberId = UUID.fromString(trimmed);
                // 앞 20%는 SELLER, 나머지 80%는 CONSUMER
                Role role = (generated < sellerCount) ? Role.SELLER : Role.CONSUMER;
                sequenceWriter.write(buildMemberRow(memberId, role));
                generated++;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy members", e);
        }
    }

    private MemberRowCsv buildMemberRow(UUID memberId, Role role) {
        String name = DUMMY_NAMES[ThreadLocalRandom.current().nextInt(DUMMY_NAMES.length)];
        String phone = PHONE_PREFIXES[ThreadLocalRandom.current().nextInt(PHONE_PREFIXES.length)]
                + "-" + (1000 + ThreadLocalRandom.current().nextInt(9000))
                + "-" + (1000 + ThreadLocalRandom.current().nextInt(9000));
        OffsetDateTime now = OffsetDateTime.now();

        return new MemberRowCsv(
                memberId,
                memberId + "@dummy.local",
                name,
                ENCODED_PASSWORD,
                phone,
                role,
                DummyDataConstants.DUMMY_SALT_KEY,
                DummyDataConstants.DUMMY_IMAGE_URL,
                now,
                now,
                null,
                Member.Status.ACTIVE
        );
    }
}
