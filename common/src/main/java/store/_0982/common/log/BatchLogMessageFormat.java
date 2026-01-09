package store._0982.common.log;

public class BatchLogMessageFormat {

    private static final String JOB_START = "[JOB] [%s] started";
    private static final String JOB_SUCCESS = "[JOB] [%s] completed";
    private static final String JOB_FAILED = "[JOB] [%s] failed";


    public static String jobStart(String jobName) {
        return String.format(JOB_START, jobName);
    }

    public static String jobSuccess(String jobName) {
        return String.format(JOB_SUCCESS, jobName);
    }

    public static String jobFailed(String jobName) {
        return String.format(JOB_FAILED, jobName);
    }

    private BatchLogMessageFormat() {}
}
