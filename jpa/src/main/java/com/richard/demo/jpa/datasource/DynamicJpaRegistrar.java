package com.richard.demo.jpa.datasource;

import com.richard.demo.basic.util.SpringContext;
import com.richard.demo.datasource.DataSourceFactory;
import com.richard.demo.datasource.log.HikariLogDataSource;
import com.richard.demo.datasource.util.DataSourceUtil;
import com.richard.demo.jpa.util.JpaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@Slf4j
public class DynamicJpaRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        for (String dataSourceName : DataSourceFactory.registerIfAbsent(registry)) {
            DataSource dataSource = (DataSource) beanFactory.getBean(dataSourceName + "DataSource");
            HikariLogDataSource logDataSource = DataSourceUtil.unwrap(dataSource, HikariLogDataSource.class);
            if (logDataSource != null) {
                log.info("Data Source JPA related beans registering, {}", dataSourceName);
                registerJpaBeans(dataSourceName, dataSource, beanFactory);
            }
        }
    }

    private void registerJpaBeans(String name, DataSource dataSource, DefaultListableBeanFactory beanFactory) {
        Environment environment = beanFactory.getBean(Environment.class);

        JpaFactory jpaFactory = beanFactory.getBean(JpaFactory.class);
        LocalContainerEntityManagerFactoryBean factoryBean = jpaFactory.entityManagerFactory(name, dataSource, environment);
        factoryBean.afterPropertiesSet();

        beanFactory.registerBeanDefinition(name + "EntityManagerFactory",
                BeanDefinitionBuilder.rootBeanDefinition(FactoryBean.class)
                        .addConstructorArgValue(factoryBean)
                        .getBeanDefinition());

        beanFactory.registerBeanDefinition(name + "TransactionManager",
                BeanDefinitionBuilder.rootBeanDefinition(JpaTransactionManager.class)
                        .addConstructorArgReference(name + "EntityManagerFactory")
                        .getBeanDefinition());
    }
}
