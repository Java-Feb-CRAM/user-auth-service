package com.smoothstack.utopia.userauthservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserAuthServiceApplication.class)
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
class UserAuthenticationApplicationTests {

  @Test
  void contextLoads() {}
}
