/**
 *
 */
package com.smoothstack.utopia.userauthservice.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
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
 * @author Craig Saunders
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
@TestInstance(Lifecycle.PER_CLASS)
class PasswordResetControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserRoleRepository userRoleRepository;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private ObjectMapper mapper;

  private final String VALID_USER_PASSWORD = "Not1CowAllowed!!!";
  private final String VALID_USER_USERNAME = "BSimpson";
  private final String INVALID_USER_PASSWORD = "Only1CowAllowed!!!";
  private final String INVALID_USER_USERNAME = "BSIMPSONNN";
  private final String NEW_PASSWORD = "Is#1!ssJAVA_feb_2021";
  private final String UNMATCHING_PASSWORD = "Is#1!ssJAVA_feb_2019";
  private final String UPDATE_PASSWORD = "/users/password/new";
  private final String GENERATE_TOKEN = "/users/password/tokens/generate";
  private final String CONFIRM_TOKEN = "/users/password/tokens/confirm";

  @BeforeEach
  private void setupDatabase() {
    mapper = new ObjectMapper();
    passwordResetTokenRepository.deleteAll();
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
    user.setUsername(VALID_USER_USERNAME);
    user.setUserRole(userRole);
    user.setPassword(passwordEncoder.encode(VALID_USER_PASSWORD));
    userRepository.save(user);
  }

  @AfterAll
  private void clearDatabase() {
    passwordResetTokenRepository.deleteAll();
    userRepository.deleteAll();
    userRoleRepository.deleteAll();
  }

  @Test
  void changeUserPassword_WithGeneratedTokenFromExistingUsername_UpdateValidPassword_Status200_AssertChangedPassword()
    throws Exception {
    String uri = GENERATE_TOKEN;
    // Get created token
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    uri = UPDATE_PASSWORD;
    // populate params
    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
    passwordResetDto.setToken(token);

    // Change password
    String inputJson = mapper.writeValueAsString(passwordResetDto);
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
      );

    assertThat(
      passwordEncoder
        .encode(NEW_PASSWORD)
        .matches(
          userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword()
        )
    );
  }

  @Test
  void changeUserPassword_WithNoToken_Status400() throws Exception {
    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);

    String uri = UPDATE_PASSWORD;

    String inputJson = mapper.writeValueAsString(passwordResetDto);
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
  void changeUserPassword_WithGeneratedTokenDeletedFromDatabase_NoPasswordUpdatePerformed_Staus400()
    throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    passwordResetTokenRepository.delete(
      passwordResetTokenRepository.findByToken(token).get()
    ); // Token deleted

    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
    passwordResetDto.setToken(token);

    uri = UPDATE_PASSWORD;

    String inputJson = mapper.writeValueAsString(passwordResetDto);
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
  void changeUserPassword_WithGeneratedTokenFromExistingUsername_InvalidCurrentPassword_NoPasswordUpdatePerformed_Staus409_AssertPasswordUnchanged()
    throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(INVALID_USER_PASSWORD); // Wrong current password
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
    passwordResetDto.setToken(token);

    uri = UPDATE_PASSWORD;

    String inputJson = mapper.writeValueAsString(passwordResetDto);
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict());

    assertThat(
      passwordEncoder
        .encode(VALID_USER_PASSWORD)
        .matches(
          userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword()
        )
    );
  }

  @Test
  void changeUserPassword_WithGeneratedTokenFromExistingUsername_NewPasswordUnmatching_NoPasswordUpdatePerformed_Staus409_AssertPasswordUnchanged()
    throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(UNMATCHING_PASSWORD); // not a match
    passwordResetDto.setToken(token);

    uri = UPDATE_PASSWORD;

    String inputJson = mapper.writeValueAsString(passwordResetDto);
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isConflict());

    assertThat(
      passwordEncoder
        .encode(VALID_USER_PASSWORD)
        .matches(
          userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword()
        )
    );
  }

  @Test
  void changeUserPassword_WithGeneratedTokenFromExistingUsername_ExpireTheToken_TokenExpired_NoPasswordUpdatePerformed_Staus400_AssertPasswordUnchanged()
    throws Exception {
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andReturn();
    String token = mapper
      .readTree(mvcResult.getResponse().getContentAsString())
      .get("token")
      .asText();

    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
    passwordResetDto.setToken(token);

    // Expire the token by one day
    PasswordResetToken passwordResetToken = passwordResetTokenRepository
      .findByToken(token)
      .get();
    passwordResetToken.setExpiryDate(
      LocalDateTime.now().minus(Duration.ofDays(1))
    );
    passwordResetTokenRepository.save(passwordResetToken);

    uri = UPDATE_PASSWORD;

    String inputJson = mapper.writeValueAsString(passwordResetDto);
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertThat(
      passwordEncoder
        .encode(VALID_USER_PASSWORD)
        .matches(
          userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword()
        )
    );
  }

  @Test
  void changeUserPassword_WithInvalidInjectedToken_TokenInvalidated_NoPasswordUpdatePerformed_Staus400_AssertPasswordUnchanged()
    throws Exception {
    // Reverse the password but don't change the token
    PasswordResetDto passwordResetDto = new PasswordResetDto();
    passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
    passwordResetDto.setNewPassword(NEW_PASSWORD);
    passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
    passwordResetDto.setToken(
      "I'm not trying to hack your system. Nope, I'm not!"
    );

    String uri = UPDATE_PASSWORD;

    String inputJson = mapper.writeValueAsString(passwordResetDto);
    // Throws error because the token was removed from the database already
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content(inputJson)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertThat(
      passwordEncoder
        .encode(VALID_USER_PASSWORD)
        .matches(
          userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword()
        )
    );
  }

  @Test
  void generatePasswordResetToken_WithValidUsername_Status201_ReturnGeneratedToken_AssertTokenExistsInDatabase()
    throws Exception {
    String uri = GENERATE_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
  }

  @Test
  void generatePasswordResetToken_WithInvalidUsername_DoNotGenerateToken_Status400_AssertNoTokenGenerated()
    throws Exception {
    String uri = GENERATE_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + INVALID_USER_USERNAME + "\"}") // username doesn't exist in database
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertThat(userRepository.findByUsername(INVALID_USER_USERNAME).isEmpty());
  }

  @Test
  void generatePasswordResetToken_GenerateTokenForUsername_GenerateAnotherTokenForSameUsername_TokenAlreadyExists_DeleteOldTokenAndGenerateNewToken_Status201_ReturnGeneratedToken_AssertOnlyOneTokenExistsInDatabaseForUsername()
    throws Exception {
    // Generate token
    String uri = GENERATE_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());

    // Generate token again
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isCreated())
      .andExpect(
        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());

    assertThat(
      passwordResetTokenRepository
        .findAllByUser(userRepository.findByUsername(VALID_USER_USERNAME).get())
        .size() ==
      1
    );
  }

  @Test
  void generatePasswordResetToken_NoUsernameSent_DoNotGenerateToken_Status400()
    throws Exception {
    // Create token
    String uri = GENERATE_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          // No content sent
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void confirmPasswordToken_GenerateTokenWithValidUsername_ConfirmTokenGenerated_UpdatePasswordInDatabase_Status200_AssertSavedTokenMatchesGeneratedToken()
    throws Exception {
    // Generate token
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
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

    // Confirm token
    uri = CONFIRM_TOKEN;
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
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    assertThat(
      token.equals(MockMvcResultMatchers.jsonPath("$.token").toString())
    );
  }

  @Test
  void confirmPassword_GenerateTokenWithValidUsername_ExpireToken_TokenExpiredAndCannotConfirm_Status400_AssertNoTokenExistsInDatabaseForUsername()
    throws Exception {
    // Generate token
    String uri = GENERATE_TOKEN;
    MvcResult mvcResult = mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"" + VALID_USER_USERNAME + "\"}")
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
    PasswordResetToken passwordResetToken = passwordResetTokenRepository
      .findByToken(token)
      .get();
    passwordResetToken.setExpiryDate(
      LocalDateTime.now().minus(Duration.ofDays(1))
    );
    passwordResetTokenRepository.save(passwordResetToken);

    // Confirm token is expired
    uri = CONFIRM_TOKEN;
    mvc
      .perform(
        MockMvcRequestBuilders
          .post(uri)
          .accept(MediaType.APPLICATION_JSON)
          .content("{\"token\":\"" + token + "\"}") // Expired token, cannot confirm
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isBadRequest());

    assertThat(
      passwordResetTokenRepository
        .findAllByUser(userRepository.findByUsername(VALID_USER_USERNAME).get())
        .size() ==
      0
    );
  }
}
