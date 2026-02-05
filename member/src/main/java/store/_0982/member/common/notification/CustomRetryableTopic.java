package store._0982.member.common.notification;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.retry.annotation.Backoff;
import store._0982.common.exception.CustomException;
import store._0982.member.exception.NegligibleKafkaException;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RetryableTopic(
        exclude = {
                CustomException.class,
                NegligibleKafkaException.class,
                DataIntegrityViolationException.class,
                IllegalArgumentException.class,
                IllegalStateException.class,
                NullPointerException.class,
                MessageConversionException.class        // 메시지를 JSON으로 파싱할 때의 예외
        },
        backoff = @Backoff(multiplier = 2.0)
)
public @interface CustomRetryableTopic {
}
