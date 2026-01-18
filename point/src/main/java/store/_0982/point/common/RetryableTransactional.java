package store._0982.point.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;
import java.net.ConnectException;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
@Retryable(
        retryFor = {TransientDataAccessException.class, QueryTimeoutException.class, ConnectException.class},
        backoff = @Backoff(delay = 500, multiplier = 2.0, maxDelay = 5000)
)
public @interface RetryableTransactional {

    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;

    @AliasFor(annotation = Transactional.class, attribute = "propagation")
    Propagation propagation() default Propagation.REQUIRED;

    @AliasFor(annotation = Transactional.class, attribute = "isolation")
    Isolation isolation() default Isolation.DEFAULT;
}
