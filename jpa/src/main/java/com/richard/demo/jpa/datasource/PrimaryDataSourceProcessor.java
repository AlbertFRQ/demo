package com.richard.demo.jpa.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Slf4j
public class PrimaryDataSourceProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanNamesForType(DataSource.class);
        if (beanNames.length < 1) {
            return;
        }
        String primaryDataSourceName = environment.getProperty("richard.datasource.default");
        if (primaryDataSourceName == null) {
            return;
        }

        setPrimaryDataSource(beanFactory, primaryDataSourceName);
        setPrimaryTransactionManager(beanFactory, primaryDataSourceName);
    }

    private void setPrimaryDataSource(ConfigurableListableBeanFactory beanFactory, String primaryDataSourceName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(primaryDataSourceName);
            beanDefinition.setPrimary(true);
        } catch (NoSuchBeanDefinitionException e) {
            log.error(String.format("Set Primary DataSource failed, no such bean definition %sDataSource", primaryDataSourceName));
            throw e;
        }
    }

    private void setPrimaryTransactionManager(ConfigurableListableBeanFactory beanFactory, String primaryDataSourceName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(primaryDataSourceName);
            beanDefinition.setPrimary(true);
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
