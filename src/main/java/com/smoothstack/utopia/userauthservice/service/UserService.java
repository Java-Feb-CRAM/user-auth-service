/**
 * 
 */
package com.smoothstack.utopia.userauthservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
import com.smoothstack.utopia.userauthservice.dao.VerificationTokenRepository;
import com.smoothstack.utopia.userauthservice.registration.dto.UserDto;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidJsonRequestException;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidRoleException;
import com.smoothstack.utopia.userauthservice.registration.error.NullTokenException;
import com.smoothstack.utopia.userauthservice.registration.error.UnmatchedPasswordException;
import com.smoothstack.utopia.userauthservice.registration.error.UserAlreadyExistException;
import com.smoothstack.utopia.userauthservice.security.UserSecurityService;

/**
 * @author Craig Saunders
 *
 */
@Service
@Transactional
public class UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;     
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;  
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;   
    @Autowired
    private SessionRegistry sessionRegistry; 
    @Autowired
    private UserSecurityService userSecurityService;
    
    public User registerNewUserAccount(UserDto userDTO) throws UserAlreadyExistException, InvalidRoleException, UnmatchedPasswordException {
        if (!userRepository.findByUsername(userDTO.getUsername()).isEmpty()) {
            throw new UserAlreadyExistException("An account already exists with that username: " + userDTO.getUsername());
        }
        if (!userRepository.findByEmail(userDTO.getEmail()).isEmpty()) {
            throw new UserAlreadyExistException("An account already exists with that email address: " + userDTO.getEmail());
        }
        
        if(userDTO.getPassword().equals(userDTO.getMatchingPassword()))
        {
            User user = new User();
            user.setGivenName(userDTO.getGivenName());
            user.setFamilyName(userDTO.getFamilyName());
            user.setUsername(userDTO.getUsername());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setUserRole(userRoleRepository.findByName(userDTO.getRole())
                    .orElseThrow(() -> new InvalidRoleException("The role given does not exist: " + userDTO.getRole())));
            return userRepository.save(new User());
        }
        else
        {
            throw new UnmatchedPasswordException("The re-typed password does not match the password given");
        }
    }

    public User getUser(String verificationToken) throws NullTokenException
    {
        VerificationToken token = verificationTokenRepository.findByToken(verificationToken).orElseThrow(NullTokenException::new);
        return token.getUser();
    }

    public void saveRegisteredUser(User user)
    {
        userRepository.save(user);
    }

    public void deleteUser(User user)
    {
        if (verificationTokenRepository.existsByUser(user)) {
            verificationTokenRepository.delete(verificationTokenRepository.findByUser(user).get());
        }
        if (passwordResetTokenRepository.existsByUser(user)) {
            passwordResetTokenRepository.delete(passwordResetTokenRepository.findByUser(user).get());
        }
        userRepository.delete(user);
    }

    public void createVerificationTokenForUser(User user, String token)
    {
        verificationTokenRepository.save(new VerificationToken(token, user));        
    }

    /**
     * @param verificationToken
     * @return
     * @throws NullPointerException
     */
    public VerificationToken generateNewVerificationToken(String verificationToken) throws NullTokenException
    {        
        VerificationToken vToken = verificationTokenRepository.findByToken(verificationToken).orElseThrow(NullTokenException::new);   
        vToken.updateToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(vToken);
        
        return verificationTokenRepository.findByToken(verificationToken).orElseThrow(NullTokenException::new);
    }

    public void createPasswordResetTokenForUser(User user, String token)
    {        
        passwordResetTokenRepository.save(new PasswordResetToken(token, user));        
    }

    public User getUserByPasswordResetToken(String token)
    {
        
    }

    public void changeUserPassword(User user, String password)
    {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public List<String> getUsersFromSessionRegistry()
    {
        return sessionRegistry.getAllPrincipals()
                .stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .map(o -> o instanceof User ? ((User) o).getEmail() : o.toString())
                .collect(Collectors.toList());
    }

    public User findUserByEmail(String email) throws InvalidJsonRequestException
    {
        return userRepository.findByEmail(email).orElseThrow(InvalidJsonRequestException::new);
    }
}
