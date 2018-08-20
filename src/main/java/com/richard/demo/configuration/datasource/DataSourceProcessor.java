package com.richard.demo.configuration.datasource;

import com.richard.demo.configuration.util.DsUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.dialect.Ingres10Dialect;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceProcessor implements BeanPostProcessor, BeanFactoryAware, EnvironmentAware {
    private ConfigurableListableBeanFactory beanFactory;
    private Environment environment;

    @Nullable
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            bean = initDataSource((DataSource) bean, beanName);
        } else if (bean instanceof LocalContainerEntityManagerFactoryBean) {
            bean = initEntityManagerFactory((LocalContainerEntityManagerFactoryBean) bean, beanName);
        }
        return bean;
    }

    private DataSource initDataSource(DataSource originDataSource, String beanName) {
        HikariDataSource dataSource = DsUtil.unwrapSilently(originDataSource, HikariDataSource.class);
        if (dataSource == null || dataSource.getPoolName() == null
                || !beanName.endsWith(dataSource.getPoolName() + "DataSource")) {
            return originDataSource;
        }
        String prefix = "synnex.datasource." + dataSource.getPoolName() + ".";

        dataSource.setJdbcUrl(environment.getProperty(prefix + "url"));
        dataSource.setDriverClassName(environment.getProperty(prefix + "driverClassName"));
        dataSource.setUsername(environment.getProperty(prefix + "userName"));
        dataSource.setPassword(environment.getProperty(prefix + "password"));
        dataSource.setMaximumPoolSize(Integer.parseInt(environment.getProperty(prefix + "maxActive", "10")));
        dataSource.setConnectionTimeout(Integer.parseInt(environment.getProperty(prefix + "maxWait", "20000")));
        //空闲的connection在连接池中被移除的时间
        dataSource.setIdleTimeout(Integer.parseInt(environment.getProperty(prefix + "minEvictableIdleTimeMillis", "300000")));
        //connection在连接池中的存活时间
        dataSource.setMaxLifetime(Integer.parseInt(environment.getProperty(prefix + "maxAge", "1800000")));
        //连接池空闲connection的最小数量
        dataSource.setMinimumIdle(Integer.parseInt(environment.getProperty(prefix + "minIdle", "0")));
        dataSource.setConnectionTestQuery(environment.getProperty(prefix + "validationQuery"));
        dataSource.setInitializationFailTimeout(-1);
        dataSource.setLeakDetectionThreshold(1800000);

        return dataSource;
    }

    private LocalContainerEntityManagerFactoryBean initEntityManagerFactory(LocalContainerEntityManagerFactoryBean emf, String beanName) {
        if ((emf.getPersistenceUnitName() == null) || !beanName.endsWith(emf.getPersistenceUnitName() + "EntityManagerFactory")) {
            return emf;
        }

        String prefix = "synnex.datasource." + emf.getPersistenceUnitName() + ".";
        String entityPackage = environment.getProperty(prefix + "entity");
        Assert.notNull(entityPackage, "Entity package is necessary!");

        DataSource dataSource = (DataSource) beanFactory.getBean(emf.getPersistenceUnitName() + "DataSource");
        emf.setDataSource(dataSource);
        emf.setJpaDialect(new HibernateJpaDialect());
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setPackagesToScan(entityPackage.substring(0, entityPackage.lastIndexOf(".")));
        emf.setJpaProperties(getJpaProperties(prefix));

        return emf;
    }

    private Properties getJpaProperties(String prefix) {
        Properties jpaProperties = new Properties();
        String url = environment.getProperty(prefix + "url");
        //SQL方言
        jpaProperties.setProperty("hibernate.dialect", environment.getProperty(prefix + "hibernate.dialect", getHibernateDialect(url)));
        //格式化SQL语句
        jpaProperties.setProperty("hibernate.format_sql", "true");

        String jpaPrefix = "spring.jpa.properties.";
        //缓存统计
        jpaProperties.setProperty("hibernate.generate_statistics", environment.getProperty(jpaPrefix + "hibernate.generate_statistics", "false"));
        jpaProperties.setProperty("hibernate.show_sql", environment.getProperty(jpaPrefix + "hibernate.show_sql", "false"));
        jpaProperties.setProperty("hibernate.use_sql_comments", environment.getProperty(jpaPrefix + "hibernate.use_sql_comments", "false"));

        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "false");

        //JDBC的Statement读取数据的时候每次从数据库中取出的记录条数
        jpaProperties.setProperty("hibernate.jdbc.fetch_size", "50");
        //对数据库进行批量删除、批量更新和批量插入的时候的批次大小
        jpaProperties.setProperty("hibernate.jdbc.batch_size", "30");
        //外连接抓取树的最大深度
        jpaProperties.setProperty("hibernate.max_fetch_depth", "3");
        jpaProperties.setProperty("hibernate.connection.release_mode", "auto");
        jpaProperties.setProperty("javax.persistence.validation.mode", "none");
        return jpaProperties;
    }

    private String getHibernateDialect(String url) {
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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
