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

    public static Object[] stepStart(
            String jobName,
            String stepName,
            Long jobExecutionId
    ) {
        return new StructuredArgument[]{
                keyValue("event", "STEP_START"),
                keyValue("job", jobName),
                keyValue("step", stepName),
                keyValue("jobExecutionId", jobExecutionId)
        };
    }

    public static Object[] stepSuccess(
            String jobName,
            String stepName,
            long readCount,
            long writeCount,
            long skipCount,
            long duration
    ) {
        return new StructuredArgument[]{
                keyValue("event", "STEP_SUCCESS"),
                keyValue("job", jobName),
                keyValue("step", stepName),
                keyValue("readCount", readCount),
                keyValue("writeCount", writeCount),
                keyValue("skipCount", skipCount),
                keyValue("duration", duration)
        };
    }

    public static Object[] stepFailed(
            String jobName,
            String stepName,
            long readCount,
            long writeCount,
            long skipCount,
            long duration,
            String errorMessage
    ) {
        return new StructuredArgument[]{
                keyValue("event", "STEP_FAILED"),
                keyValue("job", jobName),
                keyValue("step", stepName),
                keyValue("readCount", readCount),
                keyValue("writeCount", writeCount),
                keyValue("skipCount", skipCount),
                keyValue("duration", duration),
                keyValue("errorMessage", errorMessage)
        };
    }

    public static Object[] itemReaderFailed(
            String jobName,
            String stepName,
            String readerName,
            String errorType,
            String errorMessage
    ) {
        return new StructuredArgument[]{
                keyValue("event", "ITEM_READ_FAILED"),
                keyValue("job", jobName),
                keyValue("step", stepName),
                keyValue("reader", readerName),
                keyValue("errorType", errorType),
                keyValue("errorMessage", errorMessage)
        };
    }

    private BatchLogMetadataFormat() {}
}
