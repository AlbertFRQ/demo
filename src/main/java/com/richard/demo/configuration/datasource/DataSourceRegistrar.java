package com.richard.demo.configuration.datasource;

import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.Assert;

public class DataSourceRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String dataSourceName = (String) importingClassMetadata.getAllAnnotationAttributes(EnableDataSource.class.getName())
                .getFirst("value");
        Assert.hasText(dataSourceName, "DataSource can't be null!");
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setPoolName(dataSourceName);
        beanFactory.registerBeanDefinition(dataSourceName + "DataSource",
                BeanDefinitionBuilder.rootBeanDefinition(P6DataSource.class)
                        .addConstructorArgValue(hikariDataSource)
                        .getBeanDefinition());
//        without p6spy
//        beanFactory.registerBeanDefinition(dataSourceName + "DataSource",
//                BeanDefinitionBuilder.rootBeanDefinition(HikariDataSource.class)
//                        .addPropertyValue("poolName", dataSourceName)
//                        .getBeanDefinition());

        beanFactory.registerBeanDefinition(dataSourceName + "EntityManager",
                BeanDefinitionBuilder.rootBeanDefinition(LocalContainerEntityManagerFactoryBean.class)
                        .addPropertyValue("persistenceUnitName", dataSourceName)
                        .getBeanDefinition());

        beanFactory.registerBeanDefinition(dataSourceName + "TransactionManager",
                BeanDefinitionBuilder.rootBeanDefinition(JpaTransactionManager.class)
                        .addPropertyValue("persistenceUnitName", dataSourceName)
                        .getBeanDefinition());
    }
}
