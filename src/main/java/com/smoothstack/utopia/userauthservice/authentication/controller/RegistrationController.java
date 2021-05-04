package com.smoothstack.utopia.userauthservice.authentication.controller;

import com.smoothstack.utopia.shared.mailmodels.RegistrationMailModel;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.service.EmailService;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserDto;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserWithAccountVerificationTokenDto;
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
// Set all incoming and outgoing media types to json
@RequestMapping(
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = MediaType.APPLICATION_JSON_VALUE
)
public class RegistrationController {

  @Autowired
  private UserService userService;
  
  @Autowired
  private EmailService emailService;

  private static final String MAPPING_VALUE = "/users";
  private static final String NEW_USER = MAPPING_VALUE + "/new";
  private static final String GENERATE_TOKEN =
    MAPPING_VALUE + "/usernames/tokens/generate";
  private static final String ACTIVATE_USER =
    MAPPING_VALUE + "/usernames/tokens/activate";

  // New user registration
  @PostMapping(value = NEW_USER) // posting sensitive user details
  @ResponseStatus(HttpStatus.CREATED) // returns code 201 on success
  // Retrieves the validated UserDto as the entire request body
  public UserWithAccountVerificationTokenDto registerUserAccount(
    @Valid @RequestBody final UserDto userDto
  ) {
    String token = UUID.randomUUID().toString(); // Generates a simple UUID to differentiate each account activation token
    UserWithAccountVerificationTokenDto userWithToken = new UserWithAccountVerificationTokenDto(); // Prepares a DTO response
    userWithToken.setUser(userService.registerNewUserAccount(userDto)); // Creates user and adds to DTO
    userService.createVerificationTokenForUser(token, userDto.getUsername()); // Creates activation token
    userWithToken.setAccountVerificationToken(token); // Add token to DTO
    // Return full DTO which is serialized to JSON as a response
    RegistrationMailModel registrationMailModel = new RegistrationMailModel();
    registrationMailModel.setGivenName(userDto.getGivenName());
    registrationMailModel.setFamilyName(userDto.getFamilyName());
    registrationMailModel.setEmailValidationToken(token);
    emailService.send(userDto.getEmail(), EmailService.MailTemplate.REGISTRATION, registrationMailModel);
    return userWithToken;
  }

  // Generate verification token
  @PostMapping(path = GENERATE_TOKEN) // posting sensitive user details
  @ResponseStatus(HttpStatus.CREATED) // Creates a new token and adds it to the database, so 201 is the correct response
  public Map<String, String> generateVerificationToken(
    @RequestBody Map<String, String> usernameMap
  ) {
    String token = UUID.randomUUID().toString(); // Generate token
    // Deletes old token and creates a new token in database for username
    userService.createVerificationTokenForUser(
      token,
      usernameMap.get("username")
    );
    User user = userService.getUserByUsername(usernameMap.get("username"));
    RegistrationMailModel registrationMailModel = new RegistrationMailModel();
    registrationMailModel.setGivenName(user.getGivenName());
    registrationMailModel.setFamilyName(user.getFamilyName());
    registrationMailModel.setEmailValidationToken(token);
    emailService.send(user.getEmail(), EmailService.MailTemplate.REGISTRATION, registrationMailModel);
    return Collections.singletonMap("message", "email-sent"); // Return message as a singleton map to be serialized to json
  }

  // User activation with token confirmation
  @PostMapping(path = ACTIVATE_USER) // posting sensitive user details
  public Map<String, String> verifyUserAccount(
    @RequestBody Map<String, String> tokenMap
  ) {
    System.out.println("test");
    // Validate and return message for status 200
    if (userService.validateUserAccountVerificationToken(tokenMap.get("token"))
    ) {
      return Collections.singletonMap("message", "user-activated");
    }
    return Collections.singletonMap("message", "user-already-activated");
  }
}
