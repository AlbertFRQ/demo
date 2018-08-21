package com.richard.demo.configuration.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Slf4j
public class PrimaryDataSourceProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanNamesForType(DataSource.class);
        if (beanNames.length < 1) {
            return;
        }

        String primaryDataSourceName = getPrimaryDataSourceName(beanFactory);
        if (StringUtils.isEmpty(primaryDataSourceName)) {
            return;
        }

        setPrimaryDataSource(beanFactory, primaryDataSourceName);
        setPrimaryTransactionManager(beanFactory, primaryDataSourceName);
    }

    private String getPrimaryDataSourceName(ConfigurableListableBeanFactory beanFactory) {
        String primaryDataSourceName = applicationContext.getEnvironment().getProperty("richard.datasource.primary");
        if (StringUtils.isEmpty(primaryDataSourceName)) {
            if (beanFactory.containsBeanDefinition("defaultDataSource")) {
                return "default";
            }
            return null;
        } else {
            return primaryDataSourceName;
        }
    }

    private void setPrimaryDataSource(ConfigurableListableBeanFactory beanFactory, String beanName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName + "DataSource");
            beanDefinition.setPrimary(true);
            log.info("Set primary DataSource: []", beanName);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Set primary DataSource Failed.");
            throw e;
        }
    }

    private void setPrimaryTransactionManager(ConfigurableListableBeanFactory beanFactory, String beanName) {
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName + "DataSource");
            beanDefinition.setPrimary(true);
            log.info("Set primary DataSource: []", beanName);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Set primary DataSource Failed.");
            throw e;
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
