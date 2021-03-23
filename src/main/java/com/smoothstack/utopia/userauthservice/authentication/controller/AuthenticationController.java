/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.userauthservice.authentication.dto.CredentialsDto;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;
import com.smoothstack.utopia.userauthservice.authentication.util.ControllerUtil;

/**
 * @author Craig Saunders
 *
 */
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController extends ControllerUtil {
    @Autowired
    private UserService userService;

    private final String MAPPING_VALUE = "/authentication";

    @PostMapping(path = MAPPING_VALUE)
    public String authenticateUser(@Valid @RequestBody CredentialsDto credentialsDto)
    {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("status", 200);
        jsonMap.put("message", "authenticated");
        jsonMap.put("token", userService.authenticateUser(credentialsDto));
        return getJsonBodyAsString(jsonMap);
    }
}
