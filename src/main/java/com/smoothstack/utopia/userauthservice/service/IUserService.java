/**
 * 
 */
package com.smoothstack.utopia.userauthservice.service;

import java.util.List;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.web.dto.UserDTO;
import com.smoothstack.utopia.userauthservice.web.error.UserAlreadyExistException;

public interface IUserService {

    User registerNewUserAccount(UserDTO accountDto) throws UserAlreadyExistException;

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    void deleteUser(User user);

    void createVerificationTokenForUser(User user, String token);

    VerificationToken generateNewVerificationToken(String token);

    void createPasswordResetTokenForUser(User user, String token);
    
    User findUserByEmail(String email);

    User getUserByPasswordResetToken(String token);

    void changeUserPassword(User user, String password);

    String validateVerificationToken(String token);

    List<String> getUsersFromSessionRegistry();
}