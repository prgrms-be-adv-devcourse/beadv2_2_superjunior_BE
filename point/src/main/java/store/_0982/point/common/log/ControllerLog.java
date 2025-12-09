package store._0982.point.common.log;

import java.lang.annotation.*;

/**
 * 로깅이 필요한 컨트롤러 메서드에 붙이는 어노테이션입니다.
 *
 * @author Minhyung Kim
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerLog {
}
