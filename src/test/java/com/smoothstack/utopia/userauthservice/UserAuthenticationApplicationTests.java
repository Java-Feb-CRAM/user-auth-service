package com.smoothstack.utopia.userauthservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserAuthServiceApplication.class)
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
public class UserAuthenticationApplicationTests {

  @Test
  void contextLoads() {
  }

}
