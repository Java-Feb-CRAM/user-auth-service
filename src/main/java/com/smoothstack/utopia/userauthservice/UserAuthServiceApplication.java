package com.smoothstack.utopia.userauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@EntityScan(basePackages = "com.smoothstack.utopia.shared.model")
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
@PropertySource("classpath:custom.properties") // Add custom properties to the configuration
public class UserAuthServiceApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
