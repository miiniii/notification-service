package com.mh.notification.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;


import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties mainDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource mainDataSource(
            @Qualifier("mainDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate mainJdbcTemplate(
            @Qualifier("dataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConfigurationProperties("app.datasource.archive")
    public DataSourceProperties archiveDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource archiveDataSource(
            @Qualifier("archiveDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate archiveJdbcTemplate(
            @Qualifier("archiveDataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }


}