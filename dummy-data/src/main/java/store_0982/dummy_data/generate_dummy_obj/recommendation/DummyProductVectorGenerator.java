package store_0982.dummy_data.generate_dummy_obj.recommendation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;
import store._0982.recommendation.domain.product.ProductVector;
import store_0982.dummy_data.generate_dummy_obj.recommendation.row.ProductVectorCsvRow;
import store_0982.dummy_data.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
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
        CsvMapper csvMapper = CsvMapper.builder()
                .findAndAddModules()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        CsvSchema schema = csvMapper.schemaFor(ProductVectorCsvRow.class).withHeader();
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
                String line;
                int generated = 0;
                while (generated < count && (line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    UUID productId = UUID.fromString(trimmed);
                    float[] vector = randomVector(DIMENSION_SIZE);
                    ProductVector productVector = createProductVector(productId, vector);
                    String vectorLiteral = toVectorLiteral(vector);
                    sequenceWriter.write(ProductVectorCsvRow.from(productVector, vectorLiteral));
                    generated++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate dummy product vectors", e);
        }
    }

    private ProductVector createProductVector(UUID productId, float[] vector) {
        ProductEmbeddingCompletedEvent event = new ProductEmbeddingCompletedEvent(Clock.systemUTC(), productId, vector);
        ProductVector productVector = new ProductVector(event, modelVersion);
        Utils.setField(productVector, "updatedAt", OffsetDateTime.now());
        return productVector;
    }

    private static float[] randomVector(int size) {
        if (size <= 0) {
            return new float[0];
        }
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
            if (i > 0) {
                builder.append(',');
            }
            builder.append(Float.toString(vector[i]));
        }
        builder.append(']');
        return builder.toString();
    }
}
