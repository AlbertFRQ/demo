package com.richard.demo.jpa.annotation;

import com.richard.demo.jpa.datasource.DataSourceRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DataSourceRegistrar.class)
public @interface EnableDataSource {
    String value();
}
