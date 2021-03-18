package com.smoothstack.utopia.userauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan(basePackages = "com.smoothstack.utopia.shared.model")
@SpringBootApplication
public class UserAuthServiceApplication {    
    public static void main(String[] args)
    {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
