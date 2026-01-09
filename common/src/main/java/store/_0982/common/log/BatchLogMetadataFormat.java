package store._0982.common.log;

import net.logstash.logback.argument.StructuredArgument;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

public class BatchLogMetadataFormat {

    public static Object[] jobStart(String jobName, String params) {
        return new StructuredArgument[]{
                keyValue("event", "JOB_START"),
                keyValue("job", jobName),
                keyValue("params", params)
        };
    }

    public static Object[] jobSuccess(
            String jobName,
            Long executionId,
            long duration,
            String status
    ) {
        return new StructuredArgument[]{
                keyValue("event", "JOB_SUCCESS"),
                keyValue("job", jobName),
                keyValue("executionId", executionId),
                keyValue("duration", duration),
                keyValue("status", status)
        };
    }

    public static Object[] jobFailed(
            String jobName,
            Long executionId,
            long duration,
            String failedStep,
            String errorMessage
    ) {
        return new StructuredArgument[]{
                keyValue("event", "JOB_FAILED"),
                keyValue("job", jobName),
                keyValue("executionId", executionId),
                keyValue("duration", duration),
                keyValue("failedStep", failedStep),
                keyValue("errorMessage", errorMessage)
        };
    }


    private BatchLogMetadataFormat() {}
}
