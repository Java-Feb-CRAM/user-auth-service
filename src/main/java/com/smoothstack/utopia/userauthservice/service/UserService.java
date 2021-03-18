/**
 * 
 */
package com.smoothstack.utopia.userauthservice.service;

import java.util.Calendar;
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
import com.smoothstack.utopia.userauthservice.web.dto.UserDTO;
import com.smoothstack.utopia.userauthservice.web.error.InvalidRoleException;
import com.smoothstack.utopia.userauthservice.web.error.UnmatchedPasswordException;
import com.smoothstack.utopia.userauthservice.web.error.UserAlreadyExistException;

/**
 * @author Craig Saunders
 *
 */
@Service
@Transactional
public class UserService implements IUserService{
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
    
    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";
    
    public User registerNewUserAccount(final UserDTO userDTO) throws UserAlreadyExistException {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistException("There is an account with that username: " + userDTO.getUsername());
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + userDTO.getEmail());
        }
        if(userDTO.getPassword().equals(userDTO.getMatchingPassword()))
        {
            if (userRoleRepository.existsByName(userDTO.getRole()))
            {
                final User user = new User();
                user.setGivenName(userDTO.getGivenName());
                user.setFamilyName(userDTO.getFamilyName());
                user.setUsername(userDTO.getUsername());
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                user.setEmail(userDTO.getEmail());
                user.setPhone(userDTO.getPhone());
                user.setUserRole(userRoleRepository.findByName(userDTO.getRole()));
                return userRepository.save(new User());
            }
            else
            {
                throw new InvalidRoleException("The role given does not exist: " + userDTO.getRole());
            }
        }
        else
        {
            throw new UnmatchedPasswordException("The re-typed password does not match the password given");
        }
    }

    @Override
    public User getUser(final String verificationToken)
    {
        VerificationToken token = verificationTokenRepository.findByToken(verificationToken);
        if (token != null) {
            return token.getUser();
        }
        return null;
    }

    @Override
    public void saveRegisteredUser(final User user)
    {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(final User user)
    {
        if (verificationTokenRepository.existsByUser(user)) {
            verificationTokenRepository.delete(verificationTokenRepository.findByUser(user));
        }
        if (passwordResetTokenRepository.existsByUser(user)) {
            passwordResetTokenRepository.delete(passwordResetTokenRepository.findByUser(user));
        }
        userRepository.delete(user);
    }

    @Override
    public void createVerificationTokenForUser(final User user, final String token)
    {
        verificationTokenRepository.save(new VerificationToken(token, user));        
    }

    @Override
    public VerificationToken generateNewVerificationToken(final String verificationToken)
    {        
        VerificationToken token = verificationTokenRepository.findByToken(verificationToken);
        token.updateToken(UUID.randomUUID()
            .toString());
        token = verificationTokenRepository.save(token);
        return token;
    }

    @Override
    public void createPasswordResetTokenForUser(final User user, final String token)
    {        
        passwordResetTokenRepository.save(new PasswordResetToken(token, user));        
    }

    @Override
    public User getUserByPasswordResetToken(final String token)
    {
        return passwordResetTokenRepository.findByToken(token).getUser();
    }

    @Override
    public void changeUserPassword(final User user, final String password)
    {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public String validateVerificationToken(String token)
    {
        final VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
            .getTime() - cal.getTime()
            .getTime()) <= 0) {
            verificationTokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }
        
        userRepository.save(user);
        return TOKEN_VALID;
    }

    @Override
    public List<String> getUsersFromSessionRegistry()
    {
        return sessionRegistry.getAllPrincipals()
                .stream()
                .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty())
                .map(o -> o instanceof User ? ((User) o).getEmail() : o.toString())
                .collect(Collectors.toList());
    }

    @Override
    public User findUserByEmail(String email)
    {
        return userRepository.findByEmail(email);
    }
}
