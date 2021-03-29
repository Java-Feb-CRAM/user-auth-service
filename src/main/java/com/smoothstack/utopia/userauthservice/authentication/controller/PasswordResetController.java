/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;
import com.smoothstack.utopia.userauthservice.authentication.error.PasswordResetTokenExpiredException;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;

/**
 * @author Craig Saunders
 *
 */
@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class PasswordResetController {
	@Autowired
	private UserService userService;

    private final String USERNAME_REGEX = "${regex.username}"; // From custom.properties resource
    private final String MAPPING_VALUE = "/users";
    private final String NEW_PASSWORD = MAPPING_VALUE + "/password/new";
    private final String GENERATE_TOKEN = MAPPING_VALUE + "/password/tokens/generate";
    private final String CONFIRM_TOKEN = MAPPING_VALUE + "/password/tokens/confirm";

	// Change user password
	@PostMapping(value = NEW_PASSWORD)
	public Map<String, String> changeUserPassword(@Valid @RequestBody final PasswordResetDto passwordResetDto) {
		userService.changeUserPassword(passwordResetDto); // Change user password if reset token and passwords match
		return Collections.singletonMap("message", "password-reset-success");
	}

	// Generate password reset token
	@PostMapping(path = GENERATE_TOKEN)
	public Map<String, String> generatePasswordResetToken(
			@Pattern(regexp = USERNAME_REGEX) @RequestParam("username") String username) {
		String token = UUID.randomUUID().toString(); // Generate new token
		userService.createPasswordResetTokenForUser(token, username); // Save token
		return Collections.singletonMap("token", token); // Return token
	}

	// Confirm password token
	@PostMapping(path = CONFIRM_TOKEN)
	public Map<String, String> confirmPasswordToken(@RequestParam("token") String token) {
		if (!userService.validatePasswordResetToken(token)) {
			throw new PasswordResetTokenExpiredException();
		}
		return Collections.singletonMap("token", token);
	}
}
