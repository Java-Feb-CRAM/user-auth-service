/**
 * 
 */
package com.utopia.smoothstack.userauthservice.test;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Craig Saunders
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PasswordResetControllerTest{
    @Autowired
    MockMvc mvc;
    
    private final String MAPPING_VALUE = "/password-reset";
    
    private String token;    
    
    @Test
    public void getPasswordResetToken() throws Exception {       
       String uri = MAPPING_VALUE + "reset-password-token-link/idlehumor";
       MvcResult mvcResult = mvc
           .perform(MockMvcRequestBuilders.get(uri)
           .accept(MediaType.APPLICATION_JSON_VALUE))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
           .andReturn();       
       
       String content = mvcResult.getResponse().getContentAsString();
       token = (new ObjectMapper()).readValue(content, ObjectNode.class).get("token").asText();
       
    }
    @Test
    public void confirmPasswordToken() throws Exception {
       String uri = "MAPPING_VALUE + \"/confirm-password-token/"+token;
       mvc.perform(MockMvcRequestBuilders.get(uri)
           .accept(MediaType.APPLICATION_JSON_VALUE))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }
}
