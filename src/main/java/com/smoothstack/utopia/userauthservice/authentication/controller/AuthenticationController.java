/**
 *
 */
package com.smoothstack.utopia.userauthservice.authentication.controller;

import com.smoothstack.utopia.userauthservice.authentication.dto.CredentialsDto;
import com.smoothstack.utopia.userauthservice.authentication.service.UserService;
import java.util.Collections;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Craig Saunders
 *
 */
@RestController
public class AuthenticationController {

  @Autowired
  private UserService userService;

  private static final String AUTHENTICATE_USER =
    "/users/credentials/authenticate";

  @PostMapping(
    path = AUTHENTICATE_USER,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Map<String, String> authenticateUser(
    @Valid @RequestBody CredentialsDto credentialsDto
  ) {
    return Collections.singletonMap(
      "authenticatedJwt",
      userService.authenticateUser(credentialsDto)
    );
  }
}
