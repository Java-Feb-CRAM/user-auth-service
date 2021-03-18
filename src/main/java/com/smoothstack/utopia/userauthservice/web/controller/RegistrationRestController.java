/**
 * 
 */
package com.smoothstack.utopia.userauthservice.web.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.registration.OnRegistrationCompleteEvent;
import com.smoothstack.utopia.userauthservice.security.ISecurityUserService;
import com.smoothstack.utopia.userauthservice.service.UserService;
import com.smoothstack.utopia.userauthservice.web.dto.PasswordDTO;
import com.smoothstack.utopia.userauthservice.web.dto.UserDTO;
import com.smoothstack.utopia.userauthservice.web.util.GenericResponse;

/**
 * @author Craig Saunders
 *
 */
@RestController
public class RegistrationRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MessageSource messages;
    @Autowired
    private Environment env;
    @Autowired
    private ISecurityUserService securityUserService;
    
    // Registration
    @PostMapping("/user/registration")
    public GenericResponse registerUserAccount(@Valid final UserDTO userDTO, final HttpServletRequest request) {
        User registered = userService.registerNewUserAccount(userDTO);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, getAppUrl(request)));
        return new GenericResponse("success");
    }
    
    // User activation - verification
    @GetMapping("/user/resendRegistrationToken")
    public GenericResponse resendRegistrationToken(final HttpServletRequest request, @RequestParam("token") final String existingToken) {
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        User user = userService.getUser(newToken.getToken());
        mailSender.send(constructResendVerificationTokenEmail(getAppUrl(request), newToken, user));
        return new GenericResponse(messages.getMessage("message.resendToken", null, request.getLocale()));
    }
    
    // Reset password
    @PostMapping("/user/resetPassword")
    public GenericResponse resetPassword(final HttpServletRequest request, @RequestParam("email") final String userEmail) {
        final User user = userService.findUserByEmail(userEmail);
        if (user != null) {
            final String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            mailSender.send(constructPasswordResetTokenEmail(getAppUrl(request), token, user));
        }
        return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
    }
    
    // Save password
    @PostMapping("/user/savePassword")
    public GenericResponse savePassword(@Valid PasswordDTO passwordDTO) {

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
    @PostMapping("/user/updatePassword")
    public GenericResponse changeUserPassword(@Valid PasswordDTO passwordDTO) {
        final User user = userService.findUserByEmail(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        userService.changeUserPassword(user, passwordDTO.getNewPassword());
        return new GenericResponse(messages.getMessage("message.updatePasswordSuc", null, null));
    }
    
    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
    
    private SimpleMailMessage constructResendVerificationTokenEmail(final String contextPath, final VerificationToken newToken, final User user) {
        String confirmationUrl = contextPath + "/registrationConfirm.html?token=" + newToken.getToken();
        String message = messages.getMessage("message.resendToken", null, null);
        
        return constructEmail("Resend Registration Token", message + " \r\n" + confirmationUrl, user);
    }
    
    private SimpleMailMessage constructPasswordResetTokenEmail(final String contextPath, final String token, final User user) {
        final String url = contextPath + "/user/changePassword?token=" + token;
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
