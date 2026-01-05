package store._0982.elasticsearch.exception;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@Component
public class ElasticsearchExceptionTranslator {

    public CustomException translate(Exception e) {
        if (isServiceUnavailable(e)) {
            return new CustomException(CustomErrorCode.SERVICE_UNAVAILABLE);
        }
        return new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
    }

    public boolean isRetryable(Throwable e) {
        return isServiceUnavailable(e);
    }

    private boolean isServiceUnavailable(Throwable e) {
        return hasCause(e, DataAccessResourceFailureException.class)
                || hasCause(e, QueryTimeoutException.class)
                || hasCause(e, ConnectException.class)
                || hasCause(e, SocketTimeoutException.class)
                || hasCause(e, TimeoutException.class);
    }

    private boolean hasCause(Throwable e, Class<? extends Throwable> type) {
        Throwable current = e;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
