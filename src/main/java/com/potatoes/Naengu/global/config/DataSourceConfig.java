package com.potatoes.Naengu.global.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource routingDataSource(DataSource masterDataSource, DataSource slaveDataSource) {
        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(Map.of(
                DataSourceType.MASTER, masterDataSource,
                DataSourceType.SLAVE, slaveDataSource
        ));
        routing.setDefaultTargetDataSource(masterDataSource);
        return routing;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
