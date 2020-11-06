package com.richard.demo.mybatis.datasource;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.richard.demo.datasource.DataSourceConfiguration;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@AutoConfigureAfter(DataSourceConfiguration.class)
@Import(MybatisRegistrar.class)
public class MybatisConfiguration {

    @Bean
    public MybatisFactory myBatisFactory(ObjectProvider<Interceptor[]> interceptorsProvider,
                                         ObjectProvider<TypeHandler[]> typeHandlersProvider,
                                         ObjectProvider<DatabaseIdProvider> databaseIdProvider) {
        return new MybatisFactory(interceptorsProvider, typeHandlersProvider, databaseIdProvider);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<List<InnerInterceptor>> innerInterceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.setInterceptors(innerInterceptors.getIfAvailable(ArrayList::new));
        return interceptor;
    }

    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        return new PaginationInnerInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public VendorDatabaseIdProvider vendorDatabaseIdProvider() {
        VendorDatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.put("MySQL", "mysql");
        properties.put("Oracle", "oracle");
        properties.put("PostgreSQL", "postgres");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }

    @Bean
    public CustomConfigurationPostProcessor customConfigurationPostProcessor(ObjectProvider<List<ConfigurationCustomizer>> customizers) {
        return new CustomConfigurationPostProcessor(customizers);
    }

    @Bean
    public CustomConfigCustomizer customConfigCustomizer() {
        return new CustomConfigCustomizer();
    }
}
