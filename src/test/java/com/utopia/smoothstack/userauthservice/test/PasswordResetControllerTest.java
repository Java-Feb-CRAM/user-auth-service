/**
 * 
 */
package com.utopia.smoothstack.userauthservice.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smoothstack.utopia.userauthservice.UserAuthServiceApplication;
import com.smoothstack.utopia.userauthservice.authentication.controller.PasswordResetController;
import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;

/**
 * @author Craig Saunders
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(PasswordResetController.class)
public class PasswordResetControllerTest {
    @Autowired
    MockMvc mvc;

    private final String MAPPING_VALUE = "/password-reset";
    
    @Test
    public void changeUserPassword() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        String uri = MAPPING_VALUE + "/reset-password-token-link/idlehumor";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        String token = mapper.readValue(content, ObjectNode.class).get("token").asText();
        
        uri = "MAPPING_VALUE";
        // Change Password
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("CRare#1!ss_2021");
        passwordResetDto.setNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setConfirmNewPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setToken(token);
        
        String inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE).content(inputJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("password-reset"));
        
        // Reverse the password but don't change the token
        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setCurrentPassword("Is#1!ssJAVA_feb_2021");
        passwordResetDto.setNewPassword("CRare#1!ss_2021");
        passwordResetDto.setConfirmNewPassword("CRare#1!ss_2021");
        passwordResetDto.setToken(token);
        
        inputJson = mapper.writeValueAsString(passwordResetDto);
        // Throws error because the token was removed from the database already
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(inputJson)
            ).andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No token provided"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Bad Request"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.path").value(MAPPING_VALUE));
        
        // Get new created token
        uri = MAPPING_VALUE + "/reset-password-token-link/idlehumor";
        mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        content = mvcResult.getResponse().getContentAsString();
        token = mapper.readValue(content, ObjectNode.class).get("token").asText();
        // Set token this time with new created token
        passwordResetDto.setToken(token);
        inputJson = mapper.writeValueAsString(passwordResetDto);
        mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(inputJson)
            ).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("password-reset"));
    }
    
    @Test
    public void getPasswordResetToken() throws Exception
    {
        // Create token
        String uri = MAPPING_VALUE + "/reset-password-token-link/idlehumor";
        mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("token-created"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());

    }

    @Test
    public void confirmPasswordToken() throws Exception
    {
        // Get created token
        String uri = MAPPING_VALUE + "/reset-password-token-link/idlehumor";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("token-created"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists()).andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        String token = (new ObjectMapper()).readValue(content, ObjectNode.class).get("token").asText();
        
        // Confirm token
        uri = MAPPING_VALUE + "/confirm-password-token/" + token;
        mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("token-confirmed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.post-request-fields").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }
}
