package store._0982.common.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import store._0982.common.log.property.LoggingAutoProperties;

import java.util.Objects;

@Aspect
@RequiredArgsConstructor
public class LoggingAspect {

    private final LoggingAutoProperties properties;

    @Pointcut("@annotation(ControllerLog) && (@within(org.springframework.stereotype.Controller) " +
            "|| @within(org.springframework.web.bind.annotation.RestController))")
    public void controller() {
    }

    @Pointcut("@annotation(serviceLog) && @within(org.springframework.stereotype.Service)")
    public void service(ServiceLog serviceLog) {
    }

    @Around("controller()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        // enabled로 설정해 놓지 않으면 로깅 없음
        if (!properties.getController().enabled()) {
            return joinPoint.proceed();
        }

        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = attributes.getRequest();

        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();
        String memberId = request.getHeader(HeaderName.ID);

        LogFormatInfo requestLogInfo = LogFormatter.request(httpMethod, endpoint, memberId);
        log.info(requestLogInfo.message(), requestLogInfo.args());

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        HttpServletResponse response = attributes.getResponse();
        HttpStatus status = HttpStatus.valueOf(Objects.requireNonNull(response).getStatus());

        LogFormatInfo responseLogInfo = LogFormatter.response(httpMethod, endpoint, status, executionTime, memberId);
        log.info(requestLogInfo.message(), responseLogInfo.args());

        return result;
    }

    @Around(value = "service(serviceLog)", argNames = "joinPoint,serviceLog")
    public Object logService(ProceedingJoinPoint joinPoint, ServiceLog serviceLog) throws Throwable {
        // enabled로 설정해 놓지 않으면 로깅 없음
        if (!properties.getService().enabled()) {
            return joinPoint.proceed();
        }

        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();
        int threshold = getThreshold(serviceLog);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime >= threshold) {
                LogFormatInfo logFormatInfo = LogFormatter.serviceComplete(methodName, executionTime);
                log.info(logFormatInfo.message(), logFormatInfo.args());
            }
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            LogFormatInfo logFormatInfo = LogFormatter.serviceFail(methodName, executionTime);
            log.info(logFormatInfo.message(), logFormatInfo.args());
            throw e;
        }
    }

    private int getThreshold(ServiceLog serviceLog) {
        Integer propertyThreshold = properties.getService().slowThresholdMs();
        if (propertyThreshold != null) {
            return propertyThreshold;
        }
        return serviceLog.slowThresholdMs();
    }
}
