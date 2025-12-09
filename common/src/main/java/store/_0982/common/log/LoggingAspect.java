package store._0982.common.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import store._0982.common.HeaderName;

import java.util.Objects;

// TODO: 민감한 정보 동적 마킹 구현 필요 (커스텀 어노테이션 생성)
@Aspect
public class LoggingAspect {
    @Pointcut("@annotation(ControllerLog) && (@within(org.springframework.stereotype.Controller) " +
            "|| @within(org.springframework.web.bind.annotation.RestController))")
    public void controller() {
    }

    @Pointcut("@annotation(ServiceLog) && @within(org.springframework.stereotype.Service)")
    public void service() {
    }

    @Around("controller()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String memberId = request.getHeader(HeaderName.ID);
        log.atInfo().log(LogFormat.requestOf(method, uri, memberId));

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis() - startTime;

        HttpServletResponse response = attributes.getResponse();
        HttpStatus status = HttpStatus.valueOf(Objects.requireNonNull(response).getStatus());
        log.atInfo().log(LogFormat.responseOf(status, method, uri, endTime, memberId));
        return result;
    }

    @Around("service()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();
        log.atInfo().log(LogFormat.serviceStartOf(methodName, joinPoint.getArgs()));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis() - startTime;
            log.atInfo().log(LogFormat.serviceCompleteOf(methodName, endTime));
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis() - startTime;
            log.atWarn().log(LogFormat.serviceFailOf(methodName, endTime));
            throw e;
        }
    }
}
