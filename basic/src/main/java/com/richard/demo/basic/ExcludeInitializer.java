package com.richard.demo.basic;

import com.google.common.collect.Sets;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.LinkedHashMap;
import java.util.Set;

public class ExcludeInitializer implements ApplicationContextInitializer {

    private final static Set<String> EXCLUDE_SET = Sets.newHashSet(
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MapPropertySource source = getExcludeSource(environment);
        source.getSource().put("spring.autoconfigure.exclude", String.join(",", EXCLUDE_SET));
    }

    private MapPropertySource getExcludeSource(ConfigurableEnvironment configurableEnvironment) {
        MutablePropertySources sources = configurableEnvironment.getPropertySources();
        MapPropertySource source = (MapPropertySource) sources.get("defaultConfig");
        if (source == null) {
            source = new MapPropertySource("defaultConfig", new LinkedHashMap<>());
            sources.addLast(source);
        }
        return source;
    }
}
