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
        user.setUsername("BSimpson");
        user.setUserRole(userRole);
        user.setPassword(passwordEncoder.encode("Not1CowAllowed!!!"));
        userRepository.save(user);
    }

    private final String MAPPING_VALUE = "/password-reset";
    
    @Test
    public void changeUserPasswordHappyPath() throws Exception
    {
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";  
        // Get created token
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        uri = MAPPING_VALUE;
        // populate params
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Not1CowAllowed!!!");
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setToken(token);
        
        // Change password 
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE));
        
        assertThat(passwordEncoder.encode("Is#1!ssJAVA_feb_2021").matches(userRepository.findByUsername("BSimpson").get().getPassword()));      
    }
    
    @Test
    public void changeUserPasswordNegativePathTokenNotExists() throws Exception
    {           
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Not1CowAllowed!!!");
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        
        String uri = MAPPING_VALUE;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void changeUserPasswordNegativePathUsernameNotExists() throws Exception
    {        
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson"; // username does not exist     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        passwordResetTokenRepository.delete(passwordResetTokenRepository.findByToken(token).get());
        
        // Reverse the password but don't change the token
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Not1CowAllowed!!!");
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setToken(token);
        
        uri = MAPPING_VALUE;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void changeUserPasswordNegativePathCurrentPasswordInvalid() throws Exception
    {        
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Only1CowAllowed!!!"); // Wrong current password
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setToken(token);
        
        uri = MAPPING_VALUE;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void changeUserPasswordNegativePathPasswordMatchingFailed() throws Exception
    {        
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Not1CowAllowed!!!"); 
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_3333"); 
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021"); // not a match
        passwordResetDto.setToken(token);
        
        uri = MAPPING_VALUE;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    } 

    @Test
    public void changeUserPasswordNegativePathTokenExpired() throws Exception
    {        
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";     
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
        		.accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();
        
        // Reverse the password but don't change the token
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Not1CowAllowed!!!"); // Wrong current password
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setToken(token);
        
        // Expire the token by one day
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token).get();
        passwordResetToken.setExpiryDate(LocalDateTime.now().minus(Duration.ofDays(1)));
        passwordResetTokenRepository.save(passwordResetToken);    
        
        uri = MAPPING_VALUE;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void changeUserPasswordNegativePathInvalidToken() throws Exception
    {        
    	String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";
        // Reverse the password but don't change the token
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Not1CowAllowed!!!"); // Wrong password
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setToken("I'm not trying to hack your system. Nope, I'm not!");
        
        uri = MAPPING_VALUE;
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        // Throws error because the token was removed from the database already
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    
    @Test
    public void resetPasswordTokenLinkHappyPath() throws Exception
    {
        // Create token
        String uri = MAPPING_VALUE + "/reset-password-token-link/BSimpson";
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
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