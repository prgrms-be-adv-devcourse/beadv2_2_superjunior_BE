package store._0982.common.log;

public class BatchLogFormat {

    public static final String JOB_START = "[JOB_START] job={}, params={}";
    public static final String JOB_SUCCESS = "[JOB_SUCCESS] job={}, executionId={}, duration={}ms, status={}";
    public static final String JOB_FAILED = "[JOB_FAILED] job={}, executionId={}, duration={}ms, exitCode={}, error={}";

    public static final String STEP_START = "[STEP_START] step={}, jobExecutionId={}";
    public static final String STEP_SUCCESS = "[STEP_SUCCESS] step={}, read={}, write={}, duration={}ms";
    public static final String STEP_FAILED = "[STEP_FAILED] step={}, read={}, write={}, error={}";

    public static final String MONTHLY_SETTLEMENT_SUCCESS = "[BATCH_SETTLEMENT_SUCCESS] sellerId={}";
    public static final String MONTHLY_SETTLEMENT_FAILED = "[BATCH_SETTLEMENT_FAILED] sellerId={}, reason={}";

    private BatchLogFormat() {}
}
