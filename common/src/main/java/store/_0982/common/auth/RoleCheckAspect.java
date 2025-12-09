package store._0982.common.auth;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import store._0982.common.HeaderName;
import store._0982.common.exception.CustomException;
import store._0982.common.exception.DefaultErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String memberRoleHeader = request.getHeader(HeaderName.ROLE);

        if (memberRoleHeader == null) {
            throw new CustomException(DefaultErrorCode.NO_ROLE_INFO);
        }

        Role memberRole;
        try {
            memberRole = Role.valueOf(memberRoleHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(DefaultErrorCode.NO_ROLE_INFO);
        }

        if (!Arrays.asList(requireRole.value()).contains(memberRole)) {
            throw new CustomException(DefaultErrorCode.ACCESS_DENIED);
        }
    }
}
