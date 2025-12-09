package store._0982.common.log;

import java.lang.annotation.*;

/**
 * 로깅이 필요한 서비스 메서드에 붙이는 어노테이션입니다.
 *
 * @author Minhyung Kim
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLog {
}
