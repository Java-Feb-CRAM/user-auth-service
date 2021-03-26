package com.smoothstack.utopia.userauthservice.authentication.controller;

import java.util.Collections;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.userauthservice.authentication.dto.UserDto;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserWithAccountVerificationTokenDto;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;

/**
 * @author Craig Saunders
 *
 */
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class RegistrationController {
    @Autowired
    private UserService userService;

    private final String MAPPING_VALUE = "/registration";

    // Registration
    @PostMapping(value = MAPPING_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserWithAccountVerificationTokenDto registerUserAccount(@Valid @RequestBody final UserDto userDto)
    {
        String token = UUID.randomUUID().toString();
        UserWithAccountVerificationTokenDto userWithToken = new UserWithAccountVerificationTokenDto();
        userWithToken.setUser(userService.registerNewUserAccount(userDto));
        userService.createVerificationTokenForUser(token, userDto.getUsername());
        userWithToken.setAccountVerificationToken(token);
        
        return userWithToken;
    }

    // Resend user email verification token - re-send token
    @GetMapping(path = MAPPING_VALUE + "/user-account-verification-token-resend/{username}")
    public Map<String,String> recreateVerificationToken(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username)
    {
        String token = UUID.randomUUID().toString();
        userService.createVerificationTokenForUser(token, username);
        return Collections.singletonMap("token", token);
    }

    // Confirmation from token
    @GetMapping(path = MAPPING_VALUE + "/account-activation/{token}")
    public Map<String,String> verifyUserAccount(@PathVariable("token") String token)
    {

        if (userService.validateUserAccountVerificationToken(token))
        {
        	return Collections.singletonMap("message", "user-activated");
        }
    	return Collections.singletonMap("message", "user-already-activated");
    }
}