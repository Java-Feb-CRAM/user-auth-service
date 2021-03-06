/**
 *
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;
import com.smoothstack.utopia.userauthservice.authentication.error.PasswordResetTokenExpiredException;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Craig Saunders
 *
 */
@RestController
@RequestMapping(
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = MediaType.APPLICATION_JSON_VALUE
)
public class PasswordResetController {

  @Autowired
  private UserService userService;

  private static final String NEW_PASSWORD = "/users/password/new";
  private static final String GENERATE_TOKEN =
    "/users/password/tokens/generate";
  private static final String CONFIRM_TOKEN = "/users/password/tokens/confirm";
  private static final String TOKEN = "token";

  // Change user password
  @PostMapping(value = NEW_PASSWORD)
  public Map<String, String> changeUserPassword(
    @Valid @RequestBody final PasswordResetDto passwordResetDto
  ) {
    userService.changeUserPassword(passwordResetDto); // Change user password if reset token and passwords match
    return Collections.singletonMap("message", "password-reset-success");
  }

  // Generate password reset token
  @PostMapping(path = GENERATE_TOKEN)
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> generatePasswordResetToken(
    @RequestBody Map<String, String> usernameMap
  ) {
    String token = UUID.randomUUID().toString(); // Generate new token
    userService.createPasswordResetTokenForUser(
      token,
      usernameMap.get("username")
    ); // Save token
    return Collections.singletonMap(TOKEN, token); // Return token
  }

  // Confirm password token
  @PostMapping(path = CONFIRM_TOKEN)
  public Map<String, String> confirmPasswordToken(
    @RequestBody Map<String, String> tokenMap
  ) {
    if (!userService.validatePasswordResetToken(tokenMap.get(TOKEN))) {
      throw new PasswordResetTokenExpiredException();
    }
    return Collections.singletonMap(TOKEN, tokenMap.get(TOKEN));
  }
}
