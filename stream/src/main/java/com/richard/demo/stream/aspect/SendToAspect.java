package com.richard.demo.stream.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SendToAspect {

    @Pointcut("@annotation(org.springframework.messaging.handler.annotation.SendTo)")
    public void streamLog() {

    }

    @AfterReturning(pointcut = "streamLog", returning = "returnValue")
    public void afterReturning(JoinPoint joinPoint, Object returnValue) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringTypeName();
    }
}
