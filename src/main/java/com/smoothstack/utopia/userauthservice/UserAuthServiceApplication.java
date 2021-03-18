package com.smoothstack.utopia.userauthservice;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

@EntityScan(basePackages = "com.smoothstack.utopia.shared.model")
@SpringBootApplication
public class UserAuthServiceApplication {  
    /*
    private final String DB_USER = "UTOPIA_DB_USER";
    private final String DB_PASS = "UTOPIA_DB_PASS";
    private final String DB_HOST = "UTOPIA_DB_HOST";
    private final String DB_PORT = "UTOPIA_DB_PORT";
    private final String DB_NAME = "UTOPIA_DB_NAME";
    */
    private final String DB_USER = "SPRING_DB_USER";
    private final String DB_PASS = "SPRING_DB_PASS";
    private final String DB_HOST = "SPRING_DB_HOST";
    private final String DB_PORT = "SPRING_DB_PORT";
    private final String DB_NAME = "SPRING_DB_NAME";
    
    public static void main(String[] args)
    {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }

    @Bean
    public DataSource getDataSource()
    {
        return DataSourceBuilder.create().driverClassName("com.mysql.cj.jdbc.Driver").url(getDataSourceURL())
                .username(System.getenv(DB_USER)).password(System.getenv(DB_PASS)).build();
    }

    private String getDataSourceURL()
    {
        return "jdbc:mysql://" + System.getenv(DB_HOST) + ":" + System.getenv(DB_PORT) + "/"
                + System.getenv(DB_NAME);
    }
}
