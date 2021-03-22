/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.controller;

import java.util.UUID;
import java.util.stream.Stream;

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
import com.smoothstack.utopia.userauthservice.registration.error.PasswordResetTokenExpiredException;
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

    
    // Send reset password token link
    @GetMapping(path = MAPPING_VALUE + "/reset-password-token-link/{username}", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String resetPasswordTokenLink(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username) {
        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(token,username);
        // TODO: send mail via another service 
        //mailSender.send(constructEmail("Reset Password",
        //        "Your password reset link: "+ REST_ENTRYPOINT + MAPPING_VALUE + "/confirm-password-token/" + token,
        //        userService.getUserByUsername(username).getEmail()));
        return "{\"status\" : 200, \"message\" : \"email-sent\", \"token\" : \""+token+"\"}";
    }
    
    // Confirm password token before showing new password form
    @GetMapping(path = MAPPING_VALUE + "/confirm-password-token/{token}", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String confirmPasswordToken(@PathVariable("token") String token) {
        if(!userService.validatePasswordResetToken(token)) {
            throw new PasswordResetTokenExpiredException();
        }        
        StringBuilder sb = new StringBuilder();
        Stream.of(PasswordFields.values()).forEach(f -> sb.append(f.toString()+","));
        return "{\"status\" : 200, \"message\" : \"token-confirmed\", \"return_fields\" : ["+sb.toString().substring(0,sb.length()-1)+"]}";
    }
    
    // Receiving the new password form
    @PostMapping(value = MAPPING_VALUE, 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Password successfully reset")
    public String registerUserAccount(@Valid @RequestBody final PasswordDto passwordDto) {
        userService.changeUserPassword(passwordDto);
        // TODO: send mail via another service 
        //mailSender.send(constructEmail("Password Rest", 
        //        "If is was not done by you, please contact us.", 
        //        user.getEmail()));
        return "{\"status\" : 200, \"message\" : \"password-reset\"}";
    }
}
