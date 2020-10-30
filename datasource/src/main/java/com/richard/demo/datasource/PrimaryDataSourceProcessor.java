package com.richard.demo.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Slf4j
public class PrimaryDataSourceProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String primaryDataSourceName = getPrimaryDataSourceName(beanFactory);
        if (StringUtils.hasText(primaryDataSourceName)) {
            setPrimaryDataSource(beanFactory, primaryDataSourceName);
            setPrimaryTransactionManager(beanFactory, primaryDataSourceName);
            setPrimaryJdbcTemplate(beanFactory, primaryDataSourceName);
        }
    }

    private String getPrimaryDataSourceName(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanNamesForType(DataSource.class);
        if (beanNames.length == 0) {
            return null;
        }
        String configuredName = environment.getProperty("richard.datasource.primary");
        if (StringUtils.hasText(configuredName)) {
            return configuredName;
        }
        return beanFactory.containsBeanDefinition("defaultDataSource") ? "default" : null;
    }

    private void setPrimaryDataSource(ConfigurableListableBeanFactory beanFactory, String primaryDataSourceName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(primaryDataSourceName + "DataSource");
            beanDefinition.setPrimary(true);
            log.info("Set primary data source to " + primaryDataSourceName);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(String.format("Set Primary DataSource failed, no such bean definition %sDataSource", primaryDataSourceName));
            throw e;
        }
    }

    private void setPrimaryTransactionManager(ConfigurableListableBeanFactory beanFactory, String primaryDataSourceName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(primaryDataSourceName + "TransactionManager");
            beanDefinition.setPrimary(true);
            log.info("Set primary transaction manager to " + primaryDataSourceName);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(String.format("Set Primary TransactionManager failed, no such bean definition %sTransactionManager", primaryDataSourceName));
            throw e;
        }
    }

    private void setPrimaryJdbcTemplate(ConfigurableListableBeanFactory beanFactory, String primaryDataSourceName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(primaryDataSourceName + "JdbcTemplate");
            beanDefinition.setPrimary(true);
            log.info("Set primary JDBC template to " + primaryDataSourceName);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(String.format("Set Primary TransactionManager failed, no such bean definition %sTransactionManager", primaryDataSourceName));
            throw e;
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
