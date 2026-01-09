package store._0982.common.log;

public class BatchLogMessageFormat {

    private static final String JOB_START = "[JOB] [%s] started";
    private static final String JOB_SUCCESS = "[JOB] [%s] completed";
    private static final String JOB_FAILED = "[JOB] [%s] failed";

    private static final String STEP_START = "[STEP] [%s] [%s] started";
    private static final String STEP_SUCCESS = "[STEP] [%s] [%s] completed";
    private static final String STEP_FAILED = "[STEP] [%s] [%s] failed";


    public static String jobStart(String jobName) {
        return String.format(JOB_START, jobName);
    }

    public static String jobSuccess(String jobName) {
        return String.format(JOB_SUCCESS, jobName);
    }

    public static String jobFailed(String jobName) {
        return String.format(JOB_FAILED, jobName);
    }

    public static String stepStart(String jobName, String stepName) {
        return String.format(STEP_START, jobName, stepName);
    }

    public static String stepSuccess(String jobName, String stepName) {
        return String.format(STEP_SUCCESS, jobName, stepName);
    }

    public static String stepFailed(String jobName, String stepName) {
        return String.format(STEP_FAILED, jobName, stepName);
    }

    private BatchLogMessageFormat() {}
}
