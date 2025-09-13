package cz.osu.kip.favouriteLinksBE.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Aspect
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    private record HttpRequestInfo(String method, String uri) {
    }

    private static HttpRequestInfo getHttpRequestInfo() {
        HttpRequestInfo ret;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            ret = new HttpRequestInfo(request.getMethod(), request.getRequestURI());
        } catch (NullPointerException ex) {
            ret = new HttpRequestInfo("N/A", "N/A");
        }
        return ret;
    }

    @Before("execution(* cz.osu.kip.favouriteLinksBE.controllers..*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        HttpRequestInfo hri = getHttpRequestInfo();
        logger.info("HTTP {} {} - Controller method '{}.{}(...)' invoked with args: {}",
                hri.method,
                hri.uri,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* cz.osu.kip.favouriteLinksBE.controllers..*(..))", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        HttpRequestInfo hri = getHttpRequestInfo();
        String resultStr = String.valueOf(result);
        logger.info("HTTP {} {} - Controller method '{}.{}(...)' result: {}",
                hri.method,
                hri.uri,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                resultStr);
    }

    @AfterThrowing(pointcut = "execution(* cz.osu.kip.favouriteLinksBE.controllers..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        HttpRequestInfo hri = getHttpRequestInfo();
        logger.error("HTTP {} {} - Controller method '{}.{}(...)' error: {}. Args: {}",
                hri.method,
                hri.uri,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getMessage(),
                joinPoint.getArgs(),
                ex);
    }
}