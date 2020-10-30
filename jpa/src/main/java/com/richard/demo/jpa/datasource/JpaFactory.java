package com.richard.demo.jpa.datasource;

import com.richard.demo.basic.util.SpringContext;
import org.hibernate.dialect.Ingres10Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.SpringSessionContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Properties;

public class JpaFactory {

    public LocalContainerEntityManagerFactoryBean entityManagerFactory(String dataSourceName, DataSource dataSource,
                                                                       Environment environment) {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setPersistenceUnitName(dataSourceName);
        bean.setDataSource(dataSource);
        bean.setJpaDialect(new HibernateJpaDialect());
        bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        String configPrefix = "com.richard." + dataSourceName + ".";
        String[] packageToScan = StringUtils.split(SpringContext.getProperty(configPrefix, "entity", environment), ",;");

        bean.setPackagesToScan(packageToScan != null ? packageToScan : new String[0]);
        bean.setJpaProperties(getJpaProperties(configPrefix, environment));
        return bean;
    }

    private Properties getJpaProperties(String configPrefix, Environment environment) {
        Properties properties = new Properties();
        String url = SpringContext.getProperty(configPrefix, "url", environment);
        String dialect = getDialect(url);
        Assert.hasText(dialect, "Dialect required but missed: " + dialect + "hibernate.dialect");
        properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("hibernate.jdbc.use_get_generated_keys", isKeyGenSupport(url) + "");
        properties.setProperty("hibernate.current_session_context_class", SpringSessionContext.class.getName());
        properties.setProperty("hibernate.show_sql", "false");

        String jpaPrefix = "spring.jpa.properties.";
        properties.setProperty("hibernate.format_sql", SpringContext.getProperty(jpaPrefix, "hibernate.format_sql", environment));
        properties.setProperty("hibernate.use_sql_comments", SpringContext.getProperty(jpaPrefix, "hibernate.use_sql_comments", environment));
        properties.setProperty("hibernate.generate_statistics", SpringContext.getProperty(jpaPrefix, "hibernate.generate_statistics", environment));
        properties.setProperty("hibernate.cache.use_second_level_cache", SpringContext.getProperty(jpaPrefix, "hibernate.cache.use_second_level_cache", environment));
        properties.setProperty("hibernate.cache.use_query_cache", SpringContext.getProperty(jpaPrefix, "hibernate.cache.use_query_cache", environment));
        properties.setProperty("hibernate.jdbc.fetch_size", SpringContext.getProperty(jpaPrefix, "hibernate.jdbc.fetch_size", environment));
        properties.setProperty("hibernate.jdbc.batch_size", SpringContext.getProperty(jpaPrefix, "hibernate.jdbc.batch_size", environment));
        properties.setProperty("hibernate.jdbc.max_fetch_depth", SpringContext.getProperty(jpaPrefix, "hibernate.jdbc.max_fetch_depth", environment));
        properties.setProperty("hibernate.jdbc.connection.release_mode", SpringContext.getProperty(jpaPrefix, "hibernate.jdbc.connection.release_mode", environment));
        properties.setProperty("javax.persistence.validation.mode", SpringContext.getProperty(jpaPrefix, "javax.persistence.validation.mode", environment));
        properties.setProperty("hibernate.query.plan_cache_max_size", SpringContext.getProperty(jpaPrefix, "hibernate.query.plan_cache_max_size", environment));
        properties.setProperty("hibernate.query.plan_parameter_metadata_max_size", SpringContext.getProperty(jpaPrefix, "hibernate.query.plan_parameter_metadata_max_size", environment));
        properties.setProperty("hibernate.physical_naming_strategy", SpringContext.getProperty(jpaPrefix, "hibernate.physical_naming_strategy", environment));
        properties.setProperty("hibernate.implicit_naming_strategy", SpringContext.getProperty(jpaPrefix, "hibernate.implicit_naming_strategy", environment));

        return properties;
    }

    private String getDialect(String url) {
        if (url.contains(":oracle")) {
            return Oracle12cDialect.class.getName();
        }
        if (url.contains(":ingres")) {
            return Ingres10Dialect.class.getName();
        }
        if (url.contains(":mysql")) {
            return MySQL57Dialect.class.getName();
        }
        return null;
    }

    private boolean isKeyGenSupport(String url) {
        return !url.contains(":oracle") && !url.contains(":ingres");
    }
}
