/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.controller;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.registration.dto.PasswordDto;
import com.smoothstack.utopia.userauthservice.registration.dto.UserDto;
import com.smoothstack.utopia.userauthservice.registration.dto.UserFields;
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
    @Autowired
    private MailSender mailSender;
    @Autowired
    private Environment env;
    
    private final String MAPPING_VALUE = "/password-reset";
    private final String REST_ENTRYPOINT = System.getenv("SPRING_REST_ENRTYPONT");

    
    // Send reset password token link
    @GetMapping(path = MAPPING_VALUE + "/reset-password-token-link/{username}", 
            consumes = MediaType.ALL_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String resetPasswordTokenLink(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username) {
        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(username, token);
        // TODO: send mail via another service 
        mailSender.send(constructEmail("Reset Password",
                "Your password reset link: "+ REST_ENTRYPOINT + MAPPING_VALUE + "/confirm-password-token/" + token,
                userService.getUserByUsername(username).getEmail()));
        return "{\"message\" : \"email-sent\"}";
    }
    
    // Confirm password token before showing new password form
    @GetMapping(path = MAPPING_VALUE + "/confirm-password-token/{token}", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserFields cofirmPasswordToken(@RequestParam("token") String token) {
        if(!userService.validatePasswordResetToken(token)) {
            throw new InvalidTokenException();
        }        
        return new UserDto();
    }
    
    // Receiving the new password form
    @PostMapping(value = MAPPING_VALUE, 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String registerUserAccount(@Valid @RequestBody final PasswordDto passwordDto) {
        User user = userService.changeUserPassword(passwordDto);
        // TODO: send mail via another service 
        mailSender.send(constructEmail("Password Rest", 
                "If is was not done by you, please contact us.", 
                user.getEmail()));
        return "password-reset";
    }
    
    private SimpleMailMessage constructEmail(String subject, String body, String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        mailMessage.setTo(email);
        mailMessage.setFrom(env.getProperty("spring.mail.username"));
        return mailMessage;
    }
}
