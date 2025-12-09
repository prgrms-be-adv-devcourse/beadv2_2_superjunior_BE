package store._0982.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 전 사용자의 권한을 체크하는 어노테이션
 *
 * @author Huiyeong Kim
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 허용할 권한 목록 (예: Role.SELLER, Role.ADMIN)
     */
    Role[] value();
}
