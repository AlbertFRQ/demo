package com.richard.demo.mybatis.datasource;

import com.richard.demo.datasource.DataSourceFactory;
import com.richard.demo.datasource.log.HikariLogDataSource;
import com.richard.demo.datasource.util.DataSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Slf4j
public class MybatisRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        for (String dataSourceName : DataSourceFactory.registerIfAbsent(registry)) {
            DataSource dataSource = (DataSource) beanFactory.getBean(dataSourceName + "DataSource");
            HikariLogDataSource logDataSource = DataSourceUtil.unwrap(dataSource, HikariLogDataSource.class);
            if (logDataSource != null) {
                log.info("Data Source Mybatis related beans registering, {}", dataSourceName);
                registerMybatisBeans(dataSourceName, dataSource, beanFactory);
            }
        }
    }

    private void registerMybatisBeans(String dataSourceName, DataSource dataSource, DefaultListableBeanFactory beanFactory) {
        Environment environment = beanFactory.getBean(Environment.class);

        String factoryBeanName = dataSourceName + "SqlSessionFactory";
        MybatisFactory mybatisFactory = beanFactory.getBean(MybatisFactory.class);
        MybatisProperties properties = mybatisFactory.getMybatisProperties(dataSourceName, environment);
        FactoryBean<SqlSessionFactory> factoryBean = mybatisFactory.mybatisSqlSessionFactoryBean(dataSource, properties);
        beanFactory.registerSingleton(factoryBeanName, factoryBean);

        beanFactory.registerBeanDefinition(dataSourceName + "SqlSessionTemplate",
                BeanDefinitionBuilder.rootBeanDefinition(SqlSessionTemplate.class)
                        .addConstructorArgReference(factoryBeanName)
                        .addConstructorArgValue(ExecutorType.SIMPLE)
                        .getBeanDefinition());

        beanFactory.registerBeanDefinition(dataSourceName + "BatchSqlSessionTemplate",
                BeanDefinitionBuilder.rootBeanDefinition(SqlSessionTemplate.class)
                        .addConstructorArgReference(factoryBeanName)
                        .addConstructorArgValue(ExecutorType.BATCH)
                        .getBeanDefinition());

        beanFactory.registerBeanDefinition(dataSourceName + "TransactionManager",
                BeanDefinitionBuilder.rootBeanDefinition(DataSourceTransactionManager.class)
                        .addConstructorArgReference(dataSourceName + "EntityManagerFactory")
                        .getBeanDefinition());
    }
}
