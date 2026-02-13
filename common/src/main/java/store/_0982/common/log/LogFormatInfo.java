package store._0982.common.log;

import net.logstash.logback.argument.StructuredArgument;

public record LogFormatInfo(
        String message,
        Object[] args
) {
    public static LogFormatInfo of(String message, StructuredArgument... args) {
        return new LogFormatInfo(message, args);
    }
}
