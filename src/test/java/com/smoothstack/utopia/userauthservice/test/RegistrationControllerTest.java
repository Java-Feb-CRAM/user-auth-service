/**
 *
 */
package com.smoothstack.utopia.userauthservice.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserDto;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
import com.smoothstack.utopia.userauthservice.dao.VerificationTokenRepository;
import java.time.Duration;
import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MvcResult;
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
class RegistrationControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserRoleRepository userRoleRepository;

  @Autowired
  private VerificationTokenRepository verificationTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private ObjectMapper mapper;

  private User user;
  private UserRole userRole;

  private UserDto generateUserDto(
    String username,
    String email,
    String matchingPass
  ) {
    UserDto userDto = new UserDto();
    userDto.setUsername(username);
    userDto.setPhone("18887776666");
    userDto.setEmail(email);
    userDto.setFamilyName("Simpson");
    userDto.setGivenName("Homer");
    userDto.setPassword("Smrt123!");
    userDto.setMatchingPassword(matchingPass);
    return userDto;
  }

  @BeforeEach
  private void setupDatabase() {
    mapper = new ObjectMapper();

    verificationTokenRepository.deleteAll();
    userRepository.deleteAll();
    userRoleRepository.deleteAll();

    userRole = new UserRole();
    userRole.setName("ROLE_AGENT");
    userRoleRepository.save(userRole);
    userRole = new UserRole();
    userRole.setName("ROLE_ADMIN");
    userRoleRepository.save(userRole);
    userRole = new UserRole();
    userRole.setName("ROLE_USER");
    userRoleRepository.save(userRole);

    user = new User();
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
    verificationTokenRepository.deleteAll();
    userRepository.deleteAll();
    userRoleRepository.deleteAll();
  }

  private final String NEW_USER = "/users/new";
  private final String GENERATE_TOKEN = "/users/usernames/tokens/generate";
  private final String ACTIVATE_USER = "/users/useranames/tokens/activate";

  @Test
  void registerUserAccount_WithValidUserDto_Status201_AssertUserEntry()
    throws Exception {
    String uri = NEW_USER;

    UserDto userDto = new UserDto();
    userDto.setUsername("HSimpson");
    userDto.setPhone("18887776666");
    userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    userDto.setFamilyName("Simpson");
    userDto.setGivenName("Homer");
    userDto.setPassword("Smrt123!");
    userDto.setMatchingPassword("Smrt123!");
    String inputJson = mapper.writeValueAsString(userDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE)
      )
      .andExpect(
        MockMvcResultMatchers.jsonPath("$.accountVerificationToken").exists()
      );
    assertEquals(
      userRepository.findByUsername("HSimpson").get().getGivenName(),
      userDto.getGivenName()
    );
  }

  @Test
  void registerUserAccount_WithInvalidMediaType_Status400() throws Exception {
    String uri = NEW_USER;

    UserDto userDto = new UserDto();
    userDto.setUsername("HSimpson");
    userDto.setPhone("9999999999"); // Lisa's phone number
    userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    userDto.setFamilyName("Simpson");
    userDto.setGivenName("Homer");
    userDto.setPassword("Smrt123!");
    userDto.setMatchingPassword("Smrt123!");
    String inputJson = mapper.writeValueAsString(userDto);
    
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void registerUserAccount_WithAlreadyExistingPhoneNumber_Status400() throws Exception {
    String uri = NEW_USER;

    UserDto userDto = generateUserDto(
      "HSimpson",
      "sub@sandwitch.ahhhggmmmnnn.nom",
      "Smrt123!"
    );

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(userDto.toString()) // NOT JSON
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }
  
  @Test
  void registerUserAccount_WithUnmatchingPasswords_Status409()
    throws Exception {
    String uri = NEW_USER;

    UserDto userDto = new UserDto();
    userDto.setUsername("HSimpson");
    userDto.setPhone("18887776666");
    userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    userDto.setFamilyName("Simpson");
    userDto.setGivenName("Homer");
    userDto.setPassword("Smrt123!");
    userDto.setMatchingPassword("Smrt123!!");
    String inputJson = mapper.writeValueAsString(userDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict());
  }

  @Test
  void registerUserAccount_WithExistingUser_Status409() throws Exception {
    String uri = NEW_USER;

    UserDto userDto = generateUserDto(
      "BSimpson",
      "sub@sandwitch.ahhhggmmmnnn.nom",
      "Smrt123!"
    );
    String inputJson = mapper.writeValueAsString(userDto);
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict());
  }

  @Test
  void registerUserAccount_WithInvalidPasswordFormat_Status400()
    throws Exception {
    String uri = NEW_USER;

    UserDto userDto = new UserDto();
    userDto.setUsername("HSimpson");
    userDto.setPhone("18887776666");
    userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    userDto.setFamilyName("Simpson");
    userDto.setGivenName("Homer");
    userDto.setPassword("Smrt1234"); // needs special character
    userDto.setMatchingPassword("Smrt1234");
    String inputJson = mapper.writeValueAsString(userDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void registerUserAccount_WithExistingEmail_Status409() throws Exception {
    String uri = NEW_USER;

    UserDto userDto = generateUserDto("HSimpson", "test@ss.com", "Smrt123!");

    String inputJson = mapper.writeValueAsString(userDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict());
  }

  @Test
  void registerUserAccount_WithExistingUsernameMatchingLowercase_Status409()
    throws Exception {
    String uri = NEW_USER;

    UserDto userDto = generateUserDto(
      "bsimpson",
      "sub@sandwitch.ahhhggmmmnnn.nom",
      "Smrt123!"
    );
    String inputJson = mapper.writeValueAsString(userDto);

    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict());
  }

  @Test
  void generateVerificationToken_WithValidUsername_Status201()
    throws Exception {
    String uri = GENERATE_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"BSimpson\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
  }

  @Test
  void generateVerificationToken_WithValidUsername_DeleteExistingTokenAndGenerateNew_Status201_AssertOldTokenDoesNotMatchNewToken()
    throws Exception {
    String uri = GENERATE_TOKEN;

    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"BSimpson\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    uri = GENERATE_TOKEN;

    mvcResult =
      mvc
        .perform(
          MockMvcRequestBuilders
            .post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"BSimpson\"}")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isCreated())
        .andExpect(
          content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        )
        .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
        .andReturn();

    assertNotEquals(
      token,
      mapper
        .readTree(mvcResult.getResponse().getContentAsString())
        .get("token")
        .asText()
    );
  }

  @Test
  void generateVerificationToken_WithInvalidUsername_Status400()
    throws Exception {
    String uri = GENERATE_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"HSimpson\"}") // username does not exist
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void verifyUserAccount_WithValidToken_AssertNotActiveAccount_VerifyUser_Status200_AssertTokenWasRemovedAndThatUserIsActive()
    throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"BSimpson\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    assertFalse(
      verificationTokenRepository.findByToken(token).get().getUser().isActive()
    );

    uri = ACTIVATE_USER;
    mvcResult =
      mvc
        .perform(
          MockMvcRequestBuilders
            .post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\"}")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(
          content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        )
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
        .andReturn();
    String message = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("message")
      .asText();

    assertTrue(verificationTokenRepository.findByToken(token).isEmpty());
    assertTrue(userRepository.findByUsername("BSimpson").get().isActive());
    assertEquals("user-activated", message);
  }

  @Test
  void verifyUserAccount_WithAlreadyActiveUser_Status200() throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"LSimpson\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    assertTrue(userRepository.findByUsername("LSimpson").get().isActive());

    uri = ACTIVATE_USER;
    mvcResult =
      mvc
        .perform(
          MockMvcRequestBuilders
            .post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\"}")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(
          content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        )
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
        .andReturn();
    String message = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("message")
      .asText();

    assertTrue(verificationTokenRepository.findByToken(token).isEmpty());
    assertTrue(userRepository.findByUsername("LSimpson").get().isActive());
    assertEquals("user-already-activated", message);
  }

  @Test
  void verifyUserAccount_WithExpiredToken_Status400_AssertTokenRemovedAndUserWasNotActivated()
    throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"BSimpson\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    // Expire the token by one day
    VerificationToken verificationToken = verificationTokenRepository
      .findByToken(token)
      .get();
    verificationToken.setExpiryDate(
      LocalDateTime.now().minus(Duration.ofDays(1))
    );
    verificationTokenRepository.save(verificationToken);

    uri = ACTIVATE_USER;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"token\":\"" + token + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertFalse(userRepository.findByUsername("BSimpson").get().isActive());
  }
}
