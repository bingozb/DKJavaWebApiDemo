package cn.dankal.tools.api;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * API日志管理器
 */
@Component
@Aspect
public class APILogger {
    private Logger logger = Logger.getLogger(this.getClass());

    @Pointcut("execution(* cn.dankal.web.service..*(..))")
    public void apiPointcut() {}

    @Around("apiPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        try {
            long start = System.currentTimeMillis();
            result = joinPoint.proceed();
            long takeTime = System.currentTimeMillis() - start;
            logger.info(joinPoint + " Use time: " + takeTime + "ms");
        } catch (Throwable e) {
            logger.info(joinPoint + " Exception: " + e.getMessage());
        }
        return result;
    }

    //后置返回通知
    @AfterReturning(pointcut = "apiPointcut()", argNames = "joinPoint, response", returning = "response")
    public void afterReturn(JoinPoint joinPoint, APIResponse response) {
        logger.info(joinPoint + " Response: " + new Gson().toJson(response));
    }

    //抛出异常后通知
    @AfterThrowing(pointcut = "apiPointcut()", throwing = "ex")
    public void afterThrow(JoinPoint joinPoint, Exception ex) {
        logger.info(joinPoint + " Exception: " + ex.getMessage());
    }
}
