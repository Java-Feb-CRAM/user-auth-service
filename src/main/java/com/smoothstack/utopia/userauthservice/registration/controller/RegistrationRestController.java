/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.controller;

import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.registration.dto.PasswordDto;
import com.smoothstack.utopia.userauthservice.registration.dto.UserDto;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidJsonRequestException;
import com.smoothstack.utopia.userauthservice.registration.error.UserAlreadyExistException;
import com.smoothstack.utopia.userauthservice.registration.util.GenericResponse;
import com.smoothstack.utopia.userauthservice.security.UserSecurityService;
import com.smoothstack.utopia.userauthservice.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Craig Saunders
 *
 */
@RestController
@Slf4j
@RequestMapping("/registration")
public class RegistrationRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private MessageSource messages;
    @Autowired
    private Environment env;
    @Autowired
    private UserSecurityService userSecurityService;
    
    // Registration
    @PostMapping(value = "/", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED) // code 201
    public String registerUserAccount(@Valid @RequestBody final String userJson) 
            throws UserAlreadyExistException, JsonMappingException, JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();        
        UserDto userDto = mapper.readValue(userJson, UserDto.class);
        
        return mapper.writeValueAsString(userService.registerNewUserAccount(userDto));
    }
    
    // User activation - re-send token
    @GetMapping(value = "/resend-registration-token", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // code 200
    public String resendRegistrationToken(@RequestParam("token") final String token) {
        VerificationToken newToken = userService.generateNewVerificationToken(token);
        User user = userService.getUser(newToken.getToken());
        // TODO: send mail via another service 
        //mailSender.send(constructResendVerificationTokenEmail(newToken, user));
        if (user != null)
        {
            return "{\"email-success\" : \"true\"}";
        }
        else
        {
            return "{\"email-success\" : \"false\"}";
        }
    }
    
    // Reset password
    @PostMapping(value = "/reset-password", 
            consumes = MediaType.APPLICATION_JSON_VALUE, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK) // code 200
    public String resetPassword(@RequestParam("email") final String email) {
        User user = userService.findUserByEmail(email);
        if (user != null) {
            final String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);

            // TODO: send mail via another service 
            //mailSender.send(constructPasswordResetTokenEmail(token, user));
        }
        return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
    }
    
    // Save password
    @PostMapping("/savePassword")
    public GenericResponse savePassword(@Valid PasswordDto passwordDTO) {

        final String result = securityUserService.validatePasswordResetToken(passwordDTO.getToken());

        if(result != null) {
            return new GenericResponse(messages.getMessage("auth.message." + result, null, null));
        }

        User user = userService.getUserByPasswordResetToken(passwordDTO.getToken());
        if(user != null) {
            userService.changeUserPassword(user, passwordDTO.getNewPassword());
            return new GenericResponse(messages.getMessage("message.resetPasswordSuc", null, null));
        } else {
            return new GenericResponse(messages.getMessage("auth.message.invalid", null, null));
        }
    }

    // Change user password
    @PostMapping("/updatePassword")
    public GenericResponse changeUserPassword(@Valid PasswordDto passwordDTO) {
        final User user = userService.findUserByEmail(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        userService.changeUserPassword(user, passwordDTO.getNewPassword());
        return new GenericResponse(messages.getMessage("message.updatePasswordSuc", null, null));
    }
    
    private SimpleMailMessage constructResendVerificationTokenEmail(final VerificationToken newToken, final User user) {
        String confirmationUrl = "registration/registrationConfirm?token=" + newToken.getToken();
        String message = messages.getMessage("message.resendToken", null, null);
        
        return constructEmail("Resend Registration Token", message + " \r\n" + confirmationUrl, user);
    }
    
    private SimpleMailMessage constructPasswordResetTokenEmail(final String token, final User user) {
        final String url = "/user/changePassword?token=" + token;
        final String message = messages.getMessage("message.resetPassword", null, null);
        return constructEmail("Reset Password", message + " \r\n" + url, user);
    }
    
    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(env.getProperty("support.email"));
        return email;
    }
}
