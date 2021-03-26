/**
 * 
 */
package com.smoothstack.utopia.userauthservice.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserDto;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
import com.smoothstack.utopia.userauthservice.dao.VerificationTokenRepository;

/**
 * @author craig
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
  locations = "classpath:application-integrationtest.properties"
)
public class RegistrationControllerTest {
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
    
    @BeforeEach
    private void setupDatabase()
    {
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
    
    private final String MAPPING_VALUE = "/registration";
    
    @Test
    public void registerUserAccountHappyPath() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("HSimpson");
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!");
        userDto.setMatchingPassword("Smrt123!");
        String inputJson = mapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountVerificationToken").exists());
        assertThat(userDto.getGivenName().equals(userRepository.findByUsername("HSimpson").get().getGivenName()));
        assertThat(passwordEncoder.encode(userDto.getUsername())
        		.matches(userRepository.findByUsername("HSimpson").get().getPassword()));
    }
    
    @Test
    public void registerUserAccountNegativePathNonJson() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("HSimpson");
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!");
        userDto.setMatchingPassword("Smrt123!");

        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(userDto.toString()) // NOT JSON
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    
    @Test
    public void registerUserAccountNegativePathPasswordMatchingFailed() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("HSimpson");
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!");
        userDto.setMatchingPassword("Smrt123!!");
        String inputJson = mapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
    
    @Test
    public void registerUserAccountNegativePathUserExists() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("BSimpson"); // username already taken
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!");
        userDto.setMatchingPassword("Smrt123!");
        String inputJson = mapper.writeValueAsString(userDto);
        
        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        assertThat(userRepository.findByUsername("BSimpson").isPresent());
    }
    
    @Test
    public void registerUserAccountNegativePathPasswordFormatWrong() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("HSimpson");
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt1234"); // needs special character
        userDto.setMatchingPassword("Smrt1234");
        String inputJson = mapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void registerUserAccountNegativePathEmailInUseAlready() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("HSimpson");
    	userDto.setPhone("18887776666");
    	userDto.setEmail("test@ss.com"); // BSimpson's email
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!"); // needs special character
        userDto.setMatchingPassword("Smrt123!");
        String inputJson = mapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
    
    @Test
    public void registerUserAccountNegativePathUserExistsWithLowercaseUsername() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("bsimpson"); // BSimpson exists, so bsimpson should be protected too
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!");
        userDto.setMatchingPassword("Smrt123!");
        String inputJson = mapper.writeValueAsString(userDto);

        mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        assertFalse(userRepository.findByUsernameIgnoreCase("bsimpson").isEmpty());
    }
    
    @Test
	public void recreateVerificationTokenHappyPath() throws Exception {
		String uri = MAPPING_VALUE + "/user-account-verification-token-resend/BSimpson";
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
	}
    
    @Test
    public void recreateVerificationTokenHappyPathTokenExistsForUser() throws Exception 
    {
    	String uri = MAPPING_VALUE;
		
    	UserDto userDto = new UserDto();
    	userDto.setUsername("HSimpson");
    	userDto.setPhone("18887776666");
    	userDto.setEmail("sub@sandwitch.ahhhggmmmnnn.nom");
    	userDto.setFamilyName("Simpson");
        userDto.setGivenName("Homer");
        userDto.setPassword("Smrt123!");
        userDto.setMatchingPassword("Smrt123!");
        String inputJson = mapper.writeValueAsString(userDto);
        
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
            .accept(MediaType.APPLICATION_JSON)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountVerificationToken").exists()).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("accountVerificationToken").asText();        
        
        assertThat(userDto.getGivenName().equals(userRepository.findByUsername("HSimpson").get().getGivenName()));
        assertThat(passwordEncoder.matches(userDto.getUsername(), userRepository.findByUsername("HSimpson").get().getPassword()));
        
		uri = MAPPING_VALUE + "/user-account-verification-token-resend/HSimpson";
		
		mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();
        assertFalse(token.equals(mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText()));
    }
    
    @Test
	public void recreateVerificationTokenNegativePath() throws Exception {
		String uri = MAPPING_VALUE + "/user-account-verification-token-resend/HSimpson"; // username does not exist
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
	}

    @Test
	public void verifyUserAccountHappyPathHappyPath() throws Exception {
		String uri = MAPPING_VALUE + "/user-account-verification-token-resend/BSimpson";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();

        assertFalse(verificationTokenRepository
        		.findByToken(token)
        		.get()
        		.getUser()
        		.isActive());
        
        uri = MAPPING_VALUE + "/account-activation/" + token;
        mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        			.andExpect(MockMvcResultMatchers.jsonPath("$.message").exists()).andReturn();
        String message = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("message").asText();
        
        assertThat(verificationTokenRepository.findByToken(token).isEmpty());
        assertThat(userRepository.findByUsername("BSimpson").get().isActive());
        assertThat(message.equals("user-activated"));
	}
    
    @Test
	public void verifyUserAccountHappyPathNegativePath() throws Exception {
		String uri = MAPPING_VALUE + "/user-account-verification-token-resend/LSimpson";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();

        assertThat(userRepository.findByUsername("LSimpson").get().isActive());
        
        uri = MAPPING_VALUE + "/account-activation/" + token;
        mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        			.andExpect(MockMvcResultMatchers.jsonPath("$.message").exists()).andReturn();
        String message = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("message").asText();
        
        assertThat(verificationTokenRepository.findByToken(token).isEmpty());
        assertThat(userRepository.findByUsername("LSimpson").get().isActive());
        assertThat(message.equals("user-already-activated"));
	}
    
    @Test
	public void verifyUserAccountNegativePath() throws Exception {
		String uri = MAPPING_VALUE + "/user-account-verification-token-resend/BSimpson";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();
        String token = mapper.readTree(mvcResult.getResponse().getContentAsString()).get("token").asText();

        // Expire the token by one day
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).get();
        verificationToken.setExpiryDate(LocalDateTime.now().minus(Duration.ofDays(1)));
        verificationTokenRepository.save(verificationToken);
        
        uri = MAPPING_VALUE + "/account-activation/" + token;
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        
        assertThat(verificationTokenRepository.findByToken(token).isEmpty());
        assertFalse(userRepository.findByUsername("BSimpson").get().isActive());
	}
    
}