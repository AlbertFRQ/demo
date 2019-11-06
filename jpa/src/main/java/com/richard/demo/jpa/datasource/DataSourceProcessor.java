package com.richard.demo.jpa.datasource;

import com.richard.demo.jpa.util.JpaUtil;
import com.zaxxer.hikari.HikariDataSource;
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

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceProcessor implements BeanPostProcessor, BeanFactoryAware, EnvironmentAware {

    private BeanFactory beanFactory;
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

    private DataSource initDataSource(DataSource dataSource, String beanName) {
        HikariDataSource hikariDataSource = JpaUtil.unWrapper(dataSource, HikariDataSource.class);
        if (hikariDataSource == null || hikariDataSource.getPoolName() == null
                || !beanName.endsWith(hikariDataSource.getPoolName() + "DataSource")) {
            return dataSource;
        }
        String prefix = String.format("com.richard.%s.", hikariDataSource.getPoolName());

        hikariDataSource.setJdbcUrl(environment.getProperty(prefix + "url"));
        hikariDataSource.setDriverClassName(environment.getProperty(prefix + "driver"));
        hikariDataSource.setUsername(environment.getProperty(prefix + "username"));
        hikariDataSource.setPassword(environment.getProperty(prefix + "password"));
        hikariDataSource.setMaximumPoolSize(Integer.parseInt(environment.getProperty(prefix + "maxActive", "10")));
        hikariDataSource.setConnectionTimeout(Integer.parseInt(environment.getProperty(prefix + "maxWait", "20000")));
        //空闲的connection在连接池中被移除的时间
        hikariDataSource.setIdleTimeout(Integer.parseInt(environment.getProperty(prefix + "minEvictableIdleTimeMillis", "300000")));
        //connection在连接池中的存活时间
        hikariDataSource.setMaxLifetime(Integer.parseInt(environment.getProperty(prefix + "maxAge", "1800000")));
        //连接池空闲connection的最小数量
        hikariDataSource.setMinimumIdle(Integer.parseInt(environment.getProperty(prefix + "minIdle", "0")));
        hikariDataSource.setConnectionTestQuery(environment.getProperty(prefix + "validationQuery"));
        hikariDataSource.setInitializationFailTimeout(-1);
        hikariDataSource.setLeakDetectionThreshold(1800000);
        return hikariDataSource;
    }

    //call getObject() method to get EntityManagerFactory to create EntityManager
    private LocalContainerEntityManagerFactoryBean initEntityManagerFactory(LocalContainerEntityManagerFactoryBean bean, String beanName) {
        String dataSourceName = bean.getPersistenceUnitName() + "DataSource";
        if (!beanFactory.containsBean(dataSourceName)) {
            return bean;
        }
        DataSource dataSource = JpaUtil.unWrapper((DataSource) beanFactory.getBean(dataSourceName), HikariDataSource.class);
        if (dataSource == null) {
            return bean;
        }
        String prefix = String.format("com.richard.%s.", bean.getPersistenceUnitName());
        String scanPackage = environment.getProperty(prefix + "entity");

        bean.setDataSource(dataSource);
        bean.setJpaDialect(new HibernateJpaDialect());
        bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        bean.setPackagesToScan(scanPackage);
        bean.setJpaProperties(getJpaProperties(prefix));
        return bean;
    }

    private Properties getJpaProperties(String prefix) {
        Properties properties = new Properties();
        String url = environment.getProperty(prefix + "url", "url");
        //SQL方言
        properties.setProperty("hibernate.dialect", getHibernateDialect(url));
        //格式化SQL语句
        properties.setProperty("hibernate.format_sql", "true");

        String jpaPrefix = "spring.jpa.properties.";
        properties.setProperty("hibernate.generate_statistics", environment.getProperty(jpaPrefix + "hibernate.generate_statistics", "false"));
        properties.setProperty("hibernate.show_sql", environment.getProperty(jpaPrefix + "hibernate.show_sql", "false"));
        properties.setProperty("hibernate.use_sql_comments", environment.getProperty(jpaPrefix + "hibernate.use_sql_comments", "false"));
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        //JDBC的Statement读取数据的时候每次从数据库中取出的记录条数
        properties.setProperty("hibernate.jdbc.fetch_size", "50");
        //对数据库进行批量删除、批量更新和批量插入的时候的批次大小
        properties.setProperty("hibernate.jdbc.batch_size", "30");
        //外连接抓取树的最大深度
        properties.setProperty("hibernate.max_fetch_depth", "3");
        properties.setProperty("hibernate.connection.release_mode", "auto");
        properties.setProperty("javax.persistence.validation.mode", "none");
        return properties;
    }

    //Support Oracle, MySQL
    private String getHibernateDialect(String url) {
        if (url.contains(":oracle")) {
            return Oracle12cDialect.class.getName();
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
}
