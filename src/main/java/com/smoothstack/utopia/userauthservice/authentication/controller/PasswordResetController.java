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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	private final String MAPPING_VALUE = "/password-reset";

	// Receiving the new password form
	@PostMapping(value = MAPPING_VALUE)
	public Map<String, String> changeUserPassword(@Valid @RequestBody final PasswordResetDto passwordResetDto) {
		userService.changeUserPassword(passwordResetDto);
		// TODO: send email
		return Collections.singletonMap("message", "password-reset-success");
	}

	// Send reset password token link
	@GetMapping(path = MAPPING_VALUE + "/reset-password-token-link/{username}")
	public Map<String, String> resetPasswordTokenLink(
			@Pattern(regexp = "[a-zA-Z\\d_]+") @PathVariable("username") String username) {
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(token, username);
		// TODO: send email
		return Collections.singletonMap("token", token);
	}

	// Confirm password token before showing new password form
	@GetMapping(path = MAPPING_VALUE + "/confirm-password-token/{token}")
	public Map<String, String> confirmPasswordToken(@PathVariable("token") String token) {
		if (!userService.validatePasswordResetToken(token)) {
			throw new PasswordResetTokenExpiredException();
		}
		return Collections.singletonMap("token", token);
	}
}
