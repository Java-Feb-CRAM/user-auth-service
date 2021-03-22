/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserDto;
import com.smoothstack.utopia.userauthservice.authentication.error.PasswordResetTokenExpiredException;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;
import com.smoothstack.utopia.userauthservice.authentication.util.ControllerUtil;

/**
 * @author Craig Saunders
 *
 */
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistrationController extends ControllerUtil {
    @Autowired
    private UserService userService;
    
    private final String MAPPING_VALUE = "/registration";
    
    // Registration
    @PostMapping(value = MAPPING_VALUE)
    public String registerUserAccount(@Valid @RequestBody final UserDto userDto) {
        User user = userService.registerNewUserAccount(userDto);
        
        String token = UUID.randomUUID().toString();        
        userService.createVerificationTokenForUser(token, userDto.getUsername());  

        Map<String, Object> userRoleJsonMap = new HashMap<>();
        userRoleJsonMap.put("id", user.getUserRole().getId());
        userRoleJsonMap.put("name", user.getUserRole().getName());
        Map<String, Object> userJsonMap = new HashMap<>();
        userJsonMap.put("id", user.getId());
        userJsonMap.put("username", user.getUsername());
        userJsonMap.put("phone", user.getPhone());
        userJsonMap.put("email", user.getEmail());
        userJsonMap.put("familyName", user.getFamilyName());
        userJsonMap.put("givenName", user.getGivenName());
        userJsonMap.put("userRole", getJsonBodyAsString(userRoleJsonMap));
        userJsonMap.put("active", user.isActive());
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("status", 201);
        jsonBody.put("message", "inactive-user-created");
        jsonBody.put("token", token);
        jsonBody.put("User", getJsonBodyAsString(userJsonMap));
        
        return getJsonBodyAsString(jsonBody);
    }
    
    // Resend user email verification token - re-send token
    @GetMapping(path = MAPPING_VALUE + "/email-verification-resend/{username}")
    public String recreateVerificationToken(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username) {   
        String token = UUID.randomUUID().toString();        
        userService.createVerificationTokenForUser(token, username);
        
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("status", 200);
        jsonBody.put("message", "token-recreated");
        jsonBody.put("token", token);
        return getJsonBodyAsString(jsonBody);
    }
    
    // Confirmation from token
    @GetMapping(path = MAPPING_VALUE + "/email-verification/{token}")
    public String verifyEmail(@PathVariable("token") String token) {
        if(!userService.validateEmailVerificationToken(token)) {
            throw new PasswordResetTokenExpiredException();
        }
        
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("status", 200);
        jsonBody.put("message", "user-activated"); 
        return getJsonBodyAsString(jsonBody);     
    }
}
