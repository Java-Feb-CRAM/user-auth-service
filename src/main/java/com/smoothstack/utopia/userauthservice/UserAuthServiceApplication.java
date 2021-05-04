package com.smoothstack.utopia.userauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EntityScan(basePackages = "com.smoothstack.utopia.shared.model")
@ComponentScan(basePackages = {"com.smoothstack.utopia.shared", "com.smoothstack.utopia.userauthservice"})
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class })
public class UserAuthServiceApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
