package com.richard.demo.basic.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SpringContextConfiguration implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    private static volatile Boolean started;

    public SpringContextConfiguration() {
        log.info("-----------Initializing SpringContextConfiguration...");
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    static void assertInitialized() {
        Assert.state(getApplicationContext() != null, "Application context has not been initialized yet!");
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Boolean isServerStarted() {
        return started;
    }

    @EventListener
    public void applicationReadyEvent(ApplicationReadyEvent readyEvent) {
        if (isServerStartedEvent(readyEvent)) {
            started = Boolean.TRUE;
        }
    }

    static boolean isServerStartedEvent(ApplicationReadyEvent event) {
        return (event.getApplicationContext() instanceof AnnotationConfigServletWebServerApplicationContext)
                || (event.getApplicationContext() instanceof AnnotationConfigReactiveWebServerApplicationContext);
    }
}
