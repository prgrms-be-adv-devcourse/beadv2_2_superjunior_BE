package store._0982.elasticsearch.common.log;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import store._0982.elasticsearch.common.HeaderName;
import org.aspectj.lang.annotation.Aspect;

import java.util.Objects;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controller() {}

    // TODO: 서비스 계층 로깅 구현 필요
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void service() {}

    @Around("controller()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
         ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String memberId = request.getHeader(HeaderName.ID);

        long startTime = System.currentTimeMillis();
        log.info(LogFormat.requestOf(method, uri, memberId));

        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis() - startTime;
        log.info(LogFormat.responseOf(method, uri, endTime, memberId));
        return result;
    }
}
