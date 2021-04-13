/**
 *
 */
package com.smoothstack.utopia.userauthservice.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.userauthservice.authentication.dto.CredentialsDto;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @author craig
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
@TestInstance(Lifecycle.PER_CLASS)
class AuthenticationControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserRoleRepository userRoleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private ObjectMapper mapper;

  private String AUTHENTICATE_USER;

  @BeforeEach
  private void setupDatabase() {
    AUTHENTICATE_USER = "/users/credentials/authenticate";
    mapper = new ObjectMapper();
    userRepository.deleteAll();
    userRoleRepository.deleteAll();

    UserRole userRole = new UserRole();
    userRole.setName("ROLE_USER");
    userRoleRepository.save(userRole);

    User user = new User();
    user.setActive(false);
    user.setEmail("test@ss.com");
    user.setFamilyName("Simpson");
    user.setGivenName("Bart");
    user.setPhone("7777777777");
    user.setUsername("BSimpson");
    user.setUserRole(userRole);
    user.setPassword(passwordEncoder.encode("Not1CowAllowed!!!"));
    userRepository.save(user);

    user = new User();
    user.setActive(true);
    user.setEmail("sax@ss.com");
    user.setFamilyName("Simpson");
    user.setGivenName("Lisa");
    user.setPhone("9999999999");
    user.setUsername("LSimpson");
    user.setUserRole(userRole);
    user.setPassword(passwordEncoder.encode("Sax4Life!!!"));
    userRepository.save(user);
  }

  @AfterAll
  private void clearDatabase() {
    userRepository.deleteAll();
    userRoleRepository.deleteAll();
  }

  @Test
  void authenticateUserHappyPath() throws Exception {
    String uri = AUTHENTICATE_USER;
    CredentialsDto credentialsDto = new CredentialsDto();
    credentialsDto.setUsername("LSimpson");
    credentialsDto.setPassword("Sax4Life!!!");

    String inputJson = mapper.writeValueAsString(credentialsDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.authenticatedJwt").exists());

    assertTrue(userRepository.findByUsername("LSimpson").get().isActive());
    assertTrue(
      passwordEncoder.matches(
        "Sax4Life!!!",
        userRepository.findByUsername("LSimpson").get().getPassword()
      )
    );
  }

  @Test
  void authenticateUserNegativePathInactiveUser() throws Exception {
    String uri = AUTHENTICATE_USER;
    CredentialsDto credentialsDto = new CredentialsDto();
    credentialsDto.setUsername("BSimpson");
    credentialsDto.setPassword("Not1CowAllowed!!!");

    String inputJson = mapper.writeValueAsString(credentialsDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertFalse(userRepository.findByUsername("BSimpson").get().isActive());
    assertTrue(
      passwordEncoder.matches(
        "Not1CowAllowed!!!",
        userRepository.findByUsername("BSimpson").get().getPassword()
      )
    );
  }

  @Test
  void authenticateUserNegativePathUserNotExist() throws Exception {
    String uri = AUTHENTICATE_USER;
    CredentialsDto credentialsDto = new CredentialsDto();
    credentialsDto.setUsername("HSimpson"); // not HSimpson doesn't exist
    credentialsDto.setPassword("Not1CowAllowed!!!");

    String inputJson = mapper.writeValueAsString(credentialsDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertTrue(userRepository.findByUsername("HSimpson").isEmpty());
  }

  @Test
  void authenticateUserNegativePathUserPasswordNotCorrect() throws Exception {
    String uri = AUTHENTICATE_USER;
    CredentialsDto credentialsDto = new CredentialsDto();
    credentialsDto.setUsername("LSimpson");
    credentialsDto.setPassword("All10CowsAllowed!!!"); // user password not correct for LSimpson

    String inputJson = mapper.writeValueAsString(credentialsDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertTrue(userRepository.findByUsername("LSimpson").get().isActive());
    assertFalse(
      passwordEncoder
        .encode("All10CowsAllowed!!!")
        .matches(userRepository.findByUsername("LSimpson").get().getPassword())
    );
  }
}
