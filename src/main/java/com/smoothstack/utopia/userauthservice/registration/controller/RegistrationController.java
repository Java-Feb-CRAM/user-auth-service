/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.controller;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.registration.dto.UserDto;
import com.smoothstack.utopia.userauthservice.registration.error.PasswordResetTokenExpiredException;
import com.smoothstack.utopia.userauthservice.service.UserService;

/**
 * @author Craig Saunders
 *
 */
@RestController
public class RegistrationController {
    @Autowired
    private UserService userService;
    
    private final String MAPPING_VALUE = "/registration";
    
    // Registration
    @PutMapping(value = MAPPING_VALUE, 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) // code 201
    public String registerUserAccount(@Valid @RequestBody final UserDto userDto) {
        User user = userService.registerNewUserAccount(userDto);
        
        String token = UUID.randomUUID().toString();        
        userService.createVerificationTokenForUser(token, userDto.getUsername());  
        // TODO: send mail via another service 
        //mailSender.send(constructEmail("Registration Token", 
        //        REST_ENTRYPOINT + MAPPING_VALUE + "/email-verification-token/" + token, 
        //        userService.getUserByToken(token).getEmail()));
        return "{\"status\" : \"201\", \"token\" : \""+token+"\", \"message\" : \"inactive-user-created\", "+userService.dumpUserFieldsToJson(user)+"}";
    }
    
    // Resend user email verification token - re-send token
    @GetMapping(path = MAPPING_VALUE + "/email-verification-resend/{username}", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String resendEmailVerificationToken(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username) {   
        String token = UUID.randomUUID().toString();        
        userService.createVerificationTokenForUser(token, username);  
        // TODO: send mail via another service 
        //mailSender.send(constructEmail("Registration Token", 
        //        REST_ENTRYPOINT + MAPPING_VALUE + "/email-verification/" + token, 
        //        userService.getUserByToken(token).getEmail()));
        return "{\"status\" : \"200\", \"token\" : \""+token+"\", : \"message\" : \"email-sent\"}";
    }
    
    // Email confirmation from token
    @GetMapping(path = MAPPING_VALUE + "/email-verification/{token}", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String verifyEmail(@PathVariable("token") String token) {
        if(!userService.validateEmailVerificationToken(token)) {
            throw new PasswordResetTokenExpiredException();
        }
        return "{\"status\" : 200, \"message\" : \"user-activated\"]}";       
    }
    
    
/*
  @Bean
  private SimpleMailMessage constructEmail(String subject, String body, String email) {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setSubject(subject);
      mailMessage.setText(body);
      mailMessage.setTo(email);
      mailMessage.setFrom(env.getProperty("spring.mail.username"));
      return mailMessage;
  }
*/
}
