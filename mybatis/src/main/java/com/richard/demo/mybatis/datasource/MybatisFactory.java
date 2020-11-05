package com.richard.demo.mybatis.datasource;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.richard.demo.basic.util.ObjectUtil;
import com.richard.demo.basic.util.SpringContext;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public class MybatisFactory implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    private final Interceptor[] interceptors;
    private final TypeHandler[] typeHandlers;
    private final DatabaseIdProvider databaseIdProvider;

    public MybatisFactory(ObjectProvider<Interceptor[]> interceptorsProvider,
                          ObjectProvider<TypeHandler[]> typeHandlersProvider,
                          ObjectProvider<DatabaseIdProvider> databaseIdProvider) {
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.typeHandlers = typeHandlersProvider.getIfAvailable();
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
    }

    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean(DataSource dataSource, MybatisProperties properties) {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        bean.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            bean.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        if (properties.getConfigurationProperties() != null) {
            bean.setConfigurationProperties(properties.getConfigurationProperties());
        }
        if (StringUtils.hasText(properties.getTypeHandlersPackage())) {
            bean.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (StringUtils.hasText(properties.getTypeAliasesPackage())) {
            bean.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        if (properties.getTypeAliasesSuperType() != null) {
            bean.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
        }
        if (properties.resolveMapperLocations() != null) {
            bean.setMapperLocations(properties.resolveMapperLocations());
        }
        if (this.interceptors != null) {
            bean.setPlugins(this.interceptors);
        }
        if (this.typeHandlers != null) {
            bean.setTypeHandlers(this.typeHandlers);
        }
        if (this.databaseIdProvider != null) {
            bean.setDatabaseIdProvider(this.databaseIdProvider);
        }

        return bean;
    }

    public MybatisProperties getMybatisProperties(String name, Environment environment) {
        MybatisProperties properties = new MybatisProperties();

        String prefix = "richard.datasource." + name + ".mybatis.";
        properties.setConfigLocation(SpringContext.getProperty(prefix, "config-location", environment));

        String propertiesPrefix = SpringContext.getProperty(prefix, "configuration-properties", environment);
        Map<String, String> propertiesMap = SpringContext.getEnumerableProperties(environment, propertiesPrefix);
        properties.setConfigurationProperties(propertiesOf(propertiesMap));

        properties.setTypeAliasesPackage(SpringContext.getProperty(prefix, "type-aliases-package", environment));

        String superTypeClassName = SpringContext.getProperty(prefix, "type-aliases-super-type", environment);
        properties.setTypeAliasesSuperType(superTypeClassName != null ? ObjectUtil.resolveClassName(superTypeClassName) : null);

        properties.setTypeHandlersPackage(SpringContext.getProperty(prefix, "type-handlers-package", environment));

        String types = SpringContext.getProperty(prefix, "config-location", environment);
        properties.setMapperLocations(types != null ? types.split(",;") : new String[0]);

        return properties;
    }

    private static Properties propertiesOf(Map<String, String> map) {
        Properties result = new Properties();
        map.forEach(result::put);
        return result;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
