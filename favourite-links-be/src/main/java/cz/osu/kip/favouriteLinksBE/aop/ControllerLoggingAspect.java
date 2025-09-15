package cz.osu.kip.favouriteLinksBE.aop;

import jakarta.servlet.http.HttpServletRequest;
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

@Component
@Aspect
public class ControllerLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    private static HttpRequestInfo getHttpRequestInfo() {
        HttpRequestInfo ret;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null)
            ret = new HttpRequestInfo("N/A", "N/A");
        else {
            HttpServletRequest request = attributes.getRequest();
            ret = new HttpRequestInfo(request.getMethod(), request.getRequestURI());
        }
        return ret;
    }

    @Before("execution(* cz.osu.kip.favouriteLinksBE.controllers.*(..))")
    public void logBeforeController(JoinPoint joinPoint) {
        HttpRequestInfo hri = getHttpRequestInfo();
        logger.info("HTTP {} {} - Controller method '{}.{}(...)' invoked with args: {}",
                hri.method,
                hri.uri,
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* cz.osu.kip.favouriteLinksBE.controllers.*(..))", returning = "result")
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

    @AfterThrowing(pointcut = "execution(* cz.osu.kip.favouriteLinksBE.controllers.*(..))", throwing = "ex")
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

    private record HttpRequestInfo(String method, String uri) {
    }
}