package cn.dankal.tools.api;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * API日志管理器
 */
@Component
@Aspect
public class APILogger {
    private Logger logger = Logger.getLogger(this.getClass());

    @Pointcut("execution(* cn.dankal.web.service..*(..))")
    public void apiPointcut() {
    }

    @Around("apiPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        try {
            result = joinPoint.proceed();
            Object obj[] = joinPoint.getArgs();
            if (obj.length > 0) {
                APIRequest request = (APIRequest) obj[0];
                Set set = request.getParams().entrySet();
                Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
                for (Map.Entry entry : entries) {
                    logger.info("[Params] " + entry.getKey() + ":" + entry.getValue());
                }
            } else {
                logger.info("[Params] null");
            }
        } catch (Throwable e) {
            logger.info(joinPoint + " Exception: " + e.getMessage());
        }
        return result;
    }

    //后置返回通知
    @AfterReturning(pointcut = "apiPointcut()", argNames = "joinPoint, response", returning = "response")
    public void afterReturn(JoinPoint joinPoint, APIResponse response) {
        logger.info(joinPoint + " Response: " + new Gson().toJson(response) + "\n");
    }

    //抛出异常后通知
    @AfterThrowing(pointcut = "apiPointcut()", throwing = "ex")
    public void afterThrow(JoinPoint joinPoint, Exception ex) {
        logger.error(joinPoint + " Exception: " + ex.getMessage());
    }
}
