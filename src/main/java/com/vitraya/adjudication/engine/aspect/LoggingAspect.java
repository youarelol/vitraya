package com.vitraya.adjudication.engine.aspect;

import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    // Pointcut to match all methods in the com.vitraya.adjudication.engine package
    @Pointcut("execution(* com.vitraya.adjudication.engine.controller..*(..)) || execution(* com.vitraya.adjudication.engine.service..*(..))")
    public void serviceMethods() {
    }

    // Before advice
    /*@Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        // log.info("Executing method: {}", joinPoint.getSignature().toShortString());
    }*/

    // After Returning advice
    /*@AfterReturning(value = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        // log.info("Method executed: {}, Return value: {}", joinPoint.getSignature().toShortString(), result);
    }*/

    // After Throwing advice
    @AfterThrowing(value = "serviceMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in method: {}, Message: {}", joinPoint.getSignature().toShortString(), exception.getMessage());
    }

    // Around advice
    @Around("serviceMethods()")
    public Object logAround(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        if (!method.isAnnotationPresent(SkipResponseLogging.class)) {
            log.info("Starting method execution: {}", joinPoint.getSignature().toShortString());
        }

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - startTime;

        // Check for @SkipResponseLogging
        if (!method.isAnnotationPresent(SkipResponseLogging.class)) {
            log.info("Completed method execution: {} with response {} and Time taken: {} ms for method: {}",
                    methodSignature.toShortString(), result, timeTaken, method.getName());
        }

        return result;
    }
}
