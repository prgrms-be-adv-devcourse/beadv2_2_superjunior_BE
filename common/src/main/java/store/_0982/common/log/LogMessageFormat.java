package store._0982.common.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMessageFormat {

    public static final String REQUEST = "API Requested";
    public static final String RESPONSE = "API Response";
    public static final String SERVICE_COMPLETE = "Service completed";
    public static final String SERVICE_FAIL = "Service failed";
    public static final String ERROR = "error";
}
