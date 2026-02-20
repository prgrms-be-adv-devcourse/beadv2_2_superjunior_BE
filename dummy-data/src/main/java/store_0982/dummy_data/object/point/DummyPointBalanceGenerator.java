package store_0982.dummy_data.object.point;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.constants.DummyDataConstants;
import store_0982.dummy_data.object.member.dto.PointBalanceCsvRow;
import store_0982.dummy_data.util.CsvWriterUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 전체 회원(100,000명)에 대한 PointBalance CSV를 생성합니다.
 * payment_schema.point_balance 테이블에 bulk import됩니다.
 * 회원 가입 시 PointBalance가 자동 생성되지 않으면 서비스 간 데이터 불일치가 발생하므로
 * 더미 데이터에서 미리 생성합니다.
 */
@Slf4j
@Component
public class DummyPointBalanceGenerator {

    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int memberCount;
    @Value("${dummy-data.point-balance-dummy.path}")
    private String dummyPath;

    public void generate() {
        Path idPool = Path.of(memberIdPoolPath);
        Path output = Path.of(dummyPath);

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, PointBalanceCsvRow.class);

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            int generated = 0;
            String line;
            while (generated < memberCount && (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                UUID memberId = UUID.fromString(trimmed);
                sequenceWriter.write(new PointBalanceCsvRow(
                        UUID.randomUUID(),
                        memberId,
                        DummyDataConstants.POINT_BALANCE_INITIAL,
                        DummyDataConstants.POINT_BALANCE_INITIAL,
                        null,
                        0L
                ));
                generated++;
            }
            log.info("PointBalance 생성 완료: {}건", generated);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy point balances", e);
        }
    }
}
