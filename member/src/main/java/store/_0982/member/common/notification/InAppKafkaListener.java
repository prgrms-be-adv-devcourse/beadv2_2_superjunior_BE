package store._0982.member.common.notification;

import org.springframework.core.annotation.AliasFor;
import org.springframework.kafka.annotation.KafkaListener;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@KafkaListener(
        groupId = KafkaGroupIds.IN_APP,
        containerFactory = "inAppListenerContainerFactory"
)
public @interface InAppKafkaListener {

    @AliasFor(annotation = KafkaListener.class, attribute = "topics")
    String[] value() default {};
}
