package com.richard.demo.stream.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class StreamListenerAspect {

    @Pointcut("@annotation(org.springframework.messaging.handler.annotation.StreamListener)")
    public void streamLog() {

    }

    @Before("streamLog()")
    public void before(JoinPoint joinPoint) {

    }

    @After("streamLog()")
    public void after(JoinPoint joinPoint) {

    }
}
