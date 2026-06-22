package com.ncorp.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.ncorp.user_service..*.*(..))")
    public void serviceMethods(){}

    @Around("controllerMethods()")
    public Object measureExecutionTime(ProceedingJoinPoint pjp){
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            long end = System.nanoTime();
            long elapsedNs =start - end;
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
            String signature = pjp.getSignature().toShortString();
            log.info("Controller method {} executed in : {} ms", signature, elapsedMs);

        }
    }
}
