package store._0982.dummy.object.commerce;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.dummy.constants.DummyDataConstants;
import store._0982.dummy.object.commerce.row.SellerBalanceCsvRow;
import store._0982.dummy.util.CsvWriterUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * SELLER 회원(20,000명)에 대한 SellerBalance CSV를 생성합니다.
 * settlement_schema.seller_balance 테이블에 bulk import됩니다.
 * member_id_pool의 앞 20%가 SELLER이므로 동일한 범위의 ID를 사용합니다.
 */
@Slf4j
@Component
public class DummySellerBalanceGenerator {

    @Value("${dummy-data.member-id-pool.path}")
    private String memberIdPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int totalMemberCount;
    @Value("${dummy-data.seller-balance-dummy.path}")
    private String dummyPath;

    public void generate() {
        int sellerCount = (int) (totalMemberCount * DummyDataConstants.SELLER_RATIO);
        Path idPool = Path.of(memberIdPoolPath);
        Path output = Path.of(dummyPath);

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, SellerBalanceCsvRow.class);

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            int generated = 0;
            String line;
            while (generated < sellerCount && (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                UUID sellerId = UUID.fromString(trimmed);
                OffsetDateTime now = OffsetDateTime.now();
                sequenceWriter.write(new SellerBalanceCsvRow(
                        UUID.randomUUID(),
                        sellerId,
                        DummyDataConstants.SELLER_BALANCE_INITIAL,
                        now,
                        now
                ));
                generated++;
            }
            log.info("SellerBalance 생성 완료: {}건", generated);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy seller balances", e);
        }
    }
}
