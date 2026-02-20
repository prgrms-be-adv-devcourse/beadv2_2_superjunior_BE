package store._0982.dummy.object.member;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.member.domain.member.Seller;
import store._0982.dummy.constants.DummyDataConstants;
import store._0982.dummy.object.member.dto.SellerRowCsv;
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
public class DummySellerGenerator {

    @Value("${dummy-data.member-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.member-id-pool.count}")
    private int totalMemberCount;
    @Value("${dummy-data.seller-dummy.path}")
    private String dummyPath;

    private static final String[] BANK_CODES = {"004", "011", "020", "023", "027", "088", "090"};
    private static final String[] ACCOUNT_HOLDERS = {"김판매", "이상점", "박셀러", "최마켓", "정스토어"};

    public void readIdAndWriteSeller() {
        // member_id_pool의 앞 20%만 읽어 SELLER 레코드 생성
        int sellerCount = (int) (totalMemberCount * DummyDataConstants.SELLER_RATIO);
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, SellerRowCsv.class);

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            int generated = 0;
            String line;
            while (generated < sellerCount && (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                UUID sellerId = UUID.fromString(trimmed);
                sequenceWriter.write(buildSellerRow(sellerId));
                generated++;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy sellers", e);
        }
    }

    private SellerRowCsv buildSellerRow(UUID sellerId) {
        OffsetDateTime now = OffsetDateTime.now();
        String bankCode = BANK_CODES[ThreadLocalRandom.current().nextInt(BANK_CODES.length)];
        String accountNumber = String.valueOf(100_000_000L + ThreadLocalRandom.current().nextLong(900_000_000L));
        String accountHolder = ACCOUNT_HOLDERS[ThreadLocalRandom.current().nextInt(ACCOUNT_HOLDERS.length)];

        return new SellerRowCsv(
                sellerId,
                now,
                now,
                bankCode,
                accountNumber,
                accountHolder,
                toBusinessRegistrationNumber(sellerId),
                Seller.Status.ACTIVE
        );
    }

    private static String toBusinessRegistrationNumber(UUID id) {
        long value = id.getMostSignificantBits() ^ id.getLeastSignificantBits();
        String encoded = Long.toUnsignedString(value, 36).toUpperCase();
        if (encoded.length() >= 15) {
            return encoded.substring(encoded.length() - 15);
        }
        return "0".repeat(15 - encoded.length()) + encoded;
    }
}
