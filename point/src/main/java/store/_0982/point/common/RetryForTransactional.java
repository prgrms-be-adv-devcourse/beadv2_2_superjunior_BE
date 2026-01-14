package store._0982.point.common;

import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.*;
import java.net.ConnectException;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Retryable(
        retryFor = {TransientDataAccessException.class, QueryTimeoutException.class, ConnectException.class},
        backoff = @Backoff(delay = 500, multiplier = 2.0, maxDelay = 5000)
)
public @interface RetryForTransactional {
}
