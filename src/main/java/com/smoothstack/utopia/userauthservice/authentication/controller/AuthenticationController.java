/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.userauthservice.authentication.dto.CredentialsDto;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;

/**
 * @author Craig Saunders
 *
 */
@RestController
public class AuthenticationController {
	@Autowired
	private UserService userService;

	private final String MAPPING_VALUE = "/authentication";

	@PostMapping(path = MAPPING_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> authenticateUser(@Valid @RequestBody CredentialsDto credentialsDto) {
		return Collections.singletonMap("authenticated-jwt", userService.authenticateUser(credentialsDto));
	}
}
