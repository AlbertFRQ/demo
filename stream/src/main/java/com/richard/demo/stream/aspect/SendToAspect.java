package com.richard.demo.stream.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

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
        Class<?>[] parameterTypes = signature.getParameterTypes();
//        Method method = signature.getMethod();
//        SendTo annotation = method.getAnnotation(SendTo.class);
//        String[] channels = annotation.value();
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, parameterTypes);
            SendTo annotation = method.getAnnotation(SendTo.class);
            String[] channels = annotation.value();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
