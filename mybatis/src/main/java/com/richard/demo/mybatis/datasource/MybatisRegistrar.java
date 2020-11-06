package com.richard.demo.mybatis.datasource;

import com.richard.demo.basic.util.ObjectUtil;
import com.richard.demo.datasource.DataSourceFactory;
import com.richard.demo.datasource.log.HikariLogDataSource;
import com.richard.demo.datasource.util.DataSourceUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

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

        registerMapperScannerConfigurer(dataSourceName, properties.getConfigLocation(), beanFactory);
    }

    /**
     * <a href=http://mybatis.org/spring/apidocs/reference/org/mybatis/spring/mapper/MapperScannerConfigurer.html>Document</a>
     * processPropertyPlaceHolders:
     * {@link MapperScannerConfigurer#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)}
     */
    private void registerMapperScannerConfigurer(String dataSourceName, String configLocation, DefaultListableBeanFactory beanFactory) {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);

        definitionBuilder.addPropertyValue("processPropertyPlaceHolders", true);
        definitionBuilder.addPropertyValue("annotationClass", Mapper.class);
        definitionBuilder.addPropertyValue("nameGenerator", new CustomNameGenerator(dataSourceName));
        definitionBuilder.addPropertyValue("sqlSessionFactoryBeanName", dataSourceName + "SqlSessionFactory");

        definitionBuilder.addPropertyValue("basePackage", getScanPath(dataSourceName, configLocation));

        beanFactory.registerBeanDefinition(dataSourceName + "MapperScannerConfigurer", definitionBuilder.getBeanDefinition());
    }

    @SneakyThrows
    private String getScanPath(String dataSourceName, String configLocation) {
        Set<String> result = new LinkedHashSet<>();

        if (StringUtils.isEmpty(configLocation)) {
            return "";
        }

        try (InputStream in = getResourceStream(configLocation)) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            Element root = doc.getDocumentElement();

            Element mapperParent = DomUtils.getChildElementByTagName(root, "mappers");
            if (mapperParent != null) {
                DomUtils.getChildElementsByTagName(mapperParent, "mapper")
                        .forEach(element -> result.add(getScanPathFromResource(dataSourceName, element.getAttribute("resource"))));

                DomUtils.getChildElementsByTagName(mapperParent, "package")
                        .forEach(element -> result.add(element.getAttribute("name")));
            }
        } catch (Exception e) {
            log.error("failed to get mapper class from resource: {}, error: {}", configLocation, e.getMessage());
            throw e;
        }

        return String.join(",", result);
    }

    @SneakyThrows
    private String getScanPathFromResource(String dataSourceName, String resource) {
        if (StringUtils.isEmpty(resource)) {
            return "";
        }
        try (InputStream in = getResourceStream(resource)) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            Element root = doc.getDocumentElement();
            String namespace = root.getAttribute("namespace");
            if (StringUtils.isEmpty(resource)) {
                log.error("resource: {} does not config mapper class", namespace);
            }
            Class mapperClass = ObjectUtil.resolveClassName(namespace.trim());
            return mapperClass != null ? mapperClass.getPackage().getName() : "";
        } catch (Exception e) {
            log.error("failed to get mapper class from resource: {}, error: {}", resource, e.getMessage());
            throw e;
        }
    }

    @SneakyThrows
    private InputStream getResourceStream(String location) {
        return new DefaultResourceLoader().getResource(location).getInputStream();
    }
}
