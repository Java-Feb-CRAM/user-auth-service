/**
 * 
 */
package com.smoothstack.utopia.userauthservice.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;

/**
 * @author Craig Saunders
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
public class PasswordResetControllerTest {
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
    private final String NEW_PASSWORD = "Is#1!ssJAVA_feb_2021";
    private final String UNMATCHING_PASSWORD = "Is#1!ssJAVA_feb_2019";
    
    @BeforeEach
    private void setupDatabase()
    {
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

    private final String MAPPING_VALUE = "/users";
    private final String UPDATE_PASSWORD = MAPPING_VALUE + "/password/new";
    private final String GENERATE_TOKEN = MAPPING_VALUE + "/password/tokens/generate";
    private final String CONFIRM_TOKEN = MAPPING_VALUE + "/password/tokens/confirm";
    
    @Test
    public void changeUserPassword_WithGeneratedTokenFromExistingUsername_UpdateValidPassword_Status200_AssertChangedPassword() throws Exception
    {
        String uri = GENERATE_TOKEN;  
        // Get created token
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+VALID_USER_USERNAME+"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        uri = UPDATE_PASSWORD;
        // populate params
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
        passwordResetDto.setNewPassword(NEW_PASSWORD);
        passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
        passwordResetDto.setToken(token);
        
        // Change password 
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE));
        
        assertThat(passwordEncoder.encode(NEW_PASSWORD).matches(userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword()));      
    }
    
    @Test
    public void changeUserPassword_WithNoToken_Status400() throws Exception
    {           
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
        passwordResetDto.setNewPassword(NEW_PASSWORD);
        passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
        
        String uri = UPDATE_PASSWORD;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void changeUserPassword_WithGeneratedTokenDeletedFromDatabase_NoPasswordUpdatePerformed_Staus400() throws Exception
    {        
        String uri = GENERATE_TOKEN;     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+VALID_USER_USERNAME+"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        passwordResetTokenRepository.delete(passwordResetTokenRepository.findByToken(token).get()); // Token deleted
        
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
        passwordResetDto.setNewPassword(NEW_PASSWORD);
        passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
        passwordResetDto.setToken(token);
        
        uri = UPDATE_PASSWORD;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void changeUserPassword_WithGeneratedTokenFromExistingUsername_InvalidCurrentPassword_NoPasswordUpdatePerformed_Staus409_AssertPasswordUnchanged() throws Exception
    {        
        String uri = GENERATE_TOKEN;     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+VALID_USER_USERNAME+"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(INVALID_USER_PASSWORD); // Wrong current password
        passwordResetDto.setNewPassword(NEW_PASSWORD);
        passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
        passwordResetDto.setToken(token);
        
        uri = UPDATE_PASSWORD;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        
        assertThat(passwordEncoder.encode(VALID_USER_PASSWORD).matches(userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword())); 
    }

    @Test
    public void changeUserPassword_WithGeneratedTokenFromExistingUsername_NewPasswordUnmatching_NoPasswordUpdatePerformed_Staus409_AssertPasswordUnchanged() throws Exception
    {        
        String uri = GENERATE_TOKEN;     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+VALID_USER_USERNAME+"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD); 
        passwordResetDto.setNewPassword(NEW_PASSWORD); 
        passwordResetDto.setConfirmNewPassword(UNMATCHING_PASSWORD); // not a match
        passwordResetDto.setToken(token);
        
        uri = UPDATE_PASSWORD;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        
        assertThat(passwordEncoder.encode(VALID_USER_PASSWORD).matches(userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword())); 
    } 

    @Test
    public void changeUserPassword_WithGeneratedTokenFromExistingUsername_ExpireTheToken_TokenExpired_NoPasswordUpdatePerformed_Staus400_AssertPasswordUnchanged() throws Exception
    {        
        String uri = GENERATE_TOKEN;     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+VALID_USER_USERNAME+"\"}")
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
        passwordResetDto.setNewPassword(NEW_PASSWORD);
        passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
        passwordResetDto.setToken(token);
        
        // Expire the token by one day
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).get();
        passwordResetToken.setExpiryDate(LocalDateTime.now().minus(Duration.ofDays(1)));
        passwordResetTokenRepository.save(passwordResetToken);    
        
        uri = UPDATE_PASSWORD;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        assertThat(passwordEncoder.encode(VALID_USER_PASSWORD).matches(userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword())); 
    }
    
    @Test
    public void changeUserPassword_WithInvalidInjectedToken_TokenInvalidated_NoPasswordUpdatePerformed_Staus400_AssertPasswordUnchanged() throws Exception
    {        
        // Reverse the password but don't change the token
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword(VALID_USER_PASSWORD);
        passwordResetDto.setNewPassword(NEW_PASSWORD);
        passwordResetDto.setConfirmNewPassword(NEW_PASSWORD);
        passwordResetDto.setToken("I'm not trying to hack your system. Nope, I'm not!");
        
        String uri = UPDATE_PASSWORD;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        // Throws error because the token was removed from the database already
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        assertThat(passwordEncoder.encode(VALID_USER_PASSWORD).matches(userRepository.findByUsername(VALID_USER_USERNAME).get().getPassword())); 
    }
    
    
    @Test
    public void resetPasswordTokenLinkHappyPath() throws Exception
    {
        // Create token
        String uri = GENERATE_TOKEN;
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"username\":\""+VALID_USER_USERNAME+"\"}")
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }
    
    @Test
    public void resetPasswordTokenLinkNegativePathUsernameNotExists() throws Exception
    {        
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpsonnnnn"; // username does not exist     
        mvc.perform(MockMvcRequestBuilders.get(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
        			.andExpect(status().isBadRequest());
    }
    
    @Test
    public void resetPasswordTokenLinkHappyPathCreatedMultipletimes() throws Exception
    {
        // Create token
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());        

        // Create token again
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }
    
    @Test
    public void resetPasswordTokenLinkNegativePath() throws Exception
    {
        // Create token
        String uri = MAPPING_VALUE + "/reset-password-token-link/notBsimpson";
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }

    @Test
    public void confirmPasswordTokenHappyPath() throws Exception
    {
        // Get created token
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        // Confirm token
        uri = MAPPING_VALUE + "/confirm-password-token/" + token;
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
        assertThat(token.equals(MockMvcResultMatchers.jsonPath("$.token").toString()));
    }

    @Test
    public void confirmPasswordNegativePathExpiredToken() throws Exception
    {        
        // Get created token
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        // Expire the token by one day
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).get();
        passwordResetToken.setExpiryDate(LocalDateTime.now().minus(Duration.ofDays(1)));
        passwordResetTokenRepository.save(passwordResetToken);
        
        // Confirm token is expired
        uri = MAPPING_VALUE + "/confirm-password-token/" + token; // Expired token passed
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
    }
}