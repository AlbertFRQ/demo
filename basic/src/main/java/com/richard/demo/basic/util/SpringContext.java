package com.richard.demo.basic.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringContext {

    public static <T> T getBean(Class<T> type) throws BeansException {
        return getBean(SpringContextConfiguration.getApplicationContext(), type);
    }

    public static <T> T getBean(ApplicationContext applicationContext, Class<T> type) throws BeansException {
        return applicationContext.getBean(type);
    }
}
