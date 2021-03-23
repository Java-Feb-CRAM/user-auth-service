/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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

import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;
import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetFields;
import com.smoothstack.utopia.userauthservice.authentication.error.PasswordResetTokenExpiredException;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;
import com.smoothstack.utopia.userauthservice.authentication.util.ControllerUtil;

/**
 * @author Craig Saunders
 *
 */
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class PasswordResetController extends ControllerUtil {
    @Autowired
    private UserService userService;

    private final String MAPPING_VALUE = "/password-reset";

    // Receiving the new password form
    @PostMapping(value = MAPPING_VALUE)
    public String changeUserPassword(@Valid @RequestBody final PasswordResetDto passwordResetDto)
    {
        userService.changeUserPassword(passwordResetDto);
        // TODO: send email        
        Map<String, Object> jsonMap = new HashMap<>();
        List<String> returnFields = new ArrayList<>();
        Stream.of(PasswordResetFields.values()).forEach(f -> returnFields.add(f.toString()));
        jsonMap.put("status", 200);
        jsonMap.put("message", "password-reset");
        return getJsonBodyAsString(jsonMap);
    }

    // Send reset password token link
    @GetMapping(path = MAPPING_VALUE + "/reset-password-token-link/{username}")
    public String resetPasswordTokenLink(@Pattern(regexp = "[a-zA-Z]+") @PathVariable("username") String username)
    {
        String token = UUID.randomUUID().toString();
        userService.createPasswordResetTokenForUser(token, username);
        // TODO: send email
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("status", 200);
        jsonMap.put("message", "token-created");
        jsonMap.put("token", token);
        return getJsonBodyAsString(jsonMap);
    }

    // Confirm password token before showing new password form
    @GetMapping(path = MAPPING_VALUE + "/confirm-password-token/{token}")
    public String confirmPasswordToken(@PathVariable("token") String token)
    {
        if (!userService.validatePasswordResetToken(token))
        {
            throw new PasswordResetTokenExpiredException();
        }
        Map<String, Object> jsonMap = new HashMap<>();
        List<String> requestFields = new ArrayList<>();
        Stream.of(PasswordResetFields.values()).forEach(f -> requestFields.add(f.toString()));
        jsonMap.put("status", 200);
        jsonMap.put("message", "token-confirmed");
        jsonMap.put("token", token);
        jsonMap.put("post-request-fields", requestFields.toArray(new String[requestFields.size()]));
        return getJsonBodyAsString(jsonMap);
    }
}
