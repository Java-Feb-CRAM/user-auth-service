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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.userauthservice.registration.dto.PasswordDto;
import com.smoothstack.utopia.userauthservice.registration.dto.PasswordFields;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidTokenException;
import com.smoothstack.utopia.userauthservice.service.UserService;

/**
 * @author Craig Saunders
 *
 */
@RestController
public class PasswordResetController {
    @Autowired
    private UserService userService;
    
    private final String MAPPING_VALUE = "/password-reset";
    //private final String REST_ENTRYPOINT = System.getenv("SPRING_REST_ENRTYPONT");

    
    // Send reset password token link
    @GetMapping(path = MAPPING_VALUE + "/reset-password-token-link/{username}", 
            consumes = MediaType.ALL_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String resetPasswordTokenLink(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username) {
        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(token,username);
        // TODO: send mail via another service 
        //mailSender.send(constructEmail("Reset Password",
        //        "Your password reset link: "+ REST_ENTRYPOINT + MAPPING_VALUE + "/confirm-password-token/" + token,
        //        userService.getUserByUsername(username).getEmail()));
        return "{\"message\" : \"email-sent\", \"token\" : \""+token+"\"}";
    }
    
    // Confirm password token before showing new password form
    @GetMapping(path = MAPPING_VALUE + "/confirm-password-token/{token}", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PasswordFields confirmPasswordToken(@PathVariable("token") String token) {
        if(!userService.validatePasswordResetToken(token)) {
            throw new InvalidTokenException();
        }        
        return new PasswordDto();
    }
    
    // Receiving the new password form
    @PostMapping(value = MAPPING_VALUE, 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String registerUserAccount(@Valid @RequestBody final PasswordDto passwordDto) {
        userService.changeUserPassword(passwordDto);
        // TODO: send mail via another service 
        //mailSender.send(constructEmail("Password Rest", 
        //        "If is was not done by you, please contact us.", 
        //        user.getEmail()));
        return "{\"message\" : \"password-reset\"}";
    }
}
