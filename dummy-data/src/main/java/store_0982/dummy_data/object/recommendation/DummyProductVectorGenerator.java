package store_0982.dummy_data.object.recommendation;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.object.recommendation.row.ProductVectorCsvRow;
import store_0982.dummy_data.util.CsvWriterUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DummyProductVectorGenerator {

    private static final int DIMENSION_SIZE = 1536;

    @Value("${dummy-data.product-id-pool.path}")
    private String idPoolPath;
    @Value("${dummy-data.product-id-pool.count}")
    private int count;
    @Value("${dummy-data.product-vector-dummy.path}")
    private String dummyPath;
    @Value("${dummy-data.product-vector.model-version:dummy-v1}")
    private String modelVersion;

    public void readIdAndWriteProductVector() {
        Path idPool = Path.of(idPoolPath);
        Path output = Path.of(dummyPath);

        CsvMapper mapper = CsvWriterUtil.createMapper();
        CsvSchema schema = CsvWriterUtil.schemaFor(mapper, ProductVectorCsvRow.class);

        try (BufferedReader reader = Files.newBufferedReader(idPool);
             BufferedWriter writer = CsvWriterUtil.openWriter(output);
             SequenceWriter sequenceWriter = mapper.writer(schema).writeValues(writer)) {

            int generated = 0;
            String line;
            while (generated < count && (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                UUID productId = UUID.fromString(trimmed);
                float[] vector = randomVector(DIMENSION_SIZE);
                sequenceWriter.write(new ProductVectorCsvRow(
                        productId,
                        toVectorLiteral(vector),
                        modelVersion,
                        DIMENSION_SIZE,
                        OffsetDateTime.now()
                ));
                generated++;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy product vectors", e);
        }
    }

    private static float[] randomVector(int size) {
        float[] vector = new float[size];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            vector[i] = (float) random.nextDouble(-1.0, 1.0);
        }
        return vector;
    }

    private static String toVectorLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder(vector.length * 8 + 2);
        builder.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) builder.append(',');
            builder.append(Float.toString(vector[i]));
        }
        builder.append(']');
        return builder.toString();
    }
}
