package com.ncorp.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {
    @Pointcut("execution(* com.ncorp.user_service.controller.*.*(..))")
    public void controllerMethods(){}

    @Around("controllerMethods()")
    public Object measureExecutionTime(org.aspectj.lang.ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();

        try {
            return pjp.proceed();
        } finally {
            long end = System.nanoTime();
            long elapsedNs =start - end;
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
            String signature = pjp.getSignature().toShortString();
            log.info("Controller methord {} executed in : {} ms", signature, elapsedMs);

        }
    }

}
