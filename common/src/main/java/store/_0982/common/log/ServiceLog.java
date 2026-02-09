package store._0982.common.log;

import java.lang.annotation.*;

/**
 * 로깅이 필요한 서비스 메서드에 붙이는 어노테이션입니다.
 * <p>{@code common.logging.enabled}가 {@code true}인 경우에만 로그가 출력됩니다.
 *
 * @author Minhyung Kim
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLog {

    /**
     * 메서드 별로 '느린 실행 시간' 기준을 설정할 수 있습니다.<br>
     * 기준값보다 오래 걸린 경우만 로그에 출력됩니다.
     * <p>
     * {@code common.logging.slow-threshold-ms} 값이 지정되지 않은 경우만 적용됩니다.
     * <ul>
     *     <li>자연수(권장): 해당 값보다 오래 걸린 경우만 로그에 출력됩니다.</li>
     *     <li>0(기본값): 모든 경우가 로그에 출력됩니다.</li>
     * </ul>
     */
    int slowThresholdMs() default 0;
}
