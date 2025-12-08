package store._0982.point.common.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import store._0982.point.common.HeaderName;

import java.util.Objects;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    // TODO: 컨트롤러에서 거를 메서드는 거르자. 서비스도 마찬가지
    @Pointcut("@annotation(ControllerLog)")
    public void controller() {
    }

    // TODO: 서비스 계층 로깅 구현 필요
    @Pointcut("@annotation(ServiceLog))")
    public void service() {
    }

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
        HttpServletResponse response = Objects.requireNonNull(attributes).getResponse();

        HttpStatus status = HttpStatus.valueOf(Objects.requireNonNull(response).getStatus());
        long endTime = System.currentTimeMillis() - startTime;
        log.info(LogFormat.responseOf(status, method, uri, endTime, memberId));
        return result;
    }
}
