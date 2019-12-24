package com.richard.demo.stream.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

public class ManualSendAspect {

    @Pointcut("execution(public * org.springframework.messaging.MessageChannel.send(..))")
    public void streamLog() {
    }

    @Before("streamLog()")
    public void before(JoinPoint joinPoint) {

    }

    @AfterReturning("streamLog()")
    public void afterReturning(JoinPoint joinPoint) {

    }

    @AfterThrowing("streamLog()")
    public void afterThrowing(JoinPoint joinPoint) {

    }
}
