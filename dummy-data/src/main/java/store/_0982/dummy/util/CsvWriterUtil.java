package store._0982.dummy.util;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CsvWriterUtil {

    private CsvWriterUtil() {}

    public static CsvMapper createMapper() {
        CsvMapper mapper = CsvMapper.builder()
                .findAndAddModules()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static <T> CsvSchema schemaFor(CsvMapper mapper, Class<T> rowClass) {
        return mapper.schemaFor(rowClass).withHeader();
    }

    public static BufferedWriter openWriter(Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        BufferedWriter writer = Files.newBufferedWriter(outputPath);
        writer.write('\uFEFF'); // UTF-8 BOM
        return writer;
    }
}
