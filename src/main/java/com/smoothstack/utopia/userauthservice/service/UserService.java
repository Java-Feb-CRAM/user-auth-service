/**
 * 
 */
package com.smoothstack.utopia.userauthservice.service;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
import com.smoothstack.utopia.userauthservice.dao.VerificationTokenRepository;
import com.smoothstack.utopia.userauthservice.registration.dto.PasswordDto;
import com.smoothstack.utopia.userauthservice.registration.dto.UserDto;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidCurrentPasswordException;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidRoleException;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidTokenException;
import com.smoothstack.utopia.userauthservice.registration.error.InvalidUserException;
import com.smoothstack.utopia.userauthservice.registration.error.UnmatchedPasswordException;
import com.smoothstack.utopia.userauthservice.registration.error.UserAlreadyExistException;

/**
 * @author Craig Saunders
 *
 */
@Service
@Transactional
public class UserService {
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
    
    public boolean validatePasswordResetToken(String token) throws InvalidTokenException {
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(InvalidTokenException::new);        
        if ((resetToken.getExpiryDate().isBefore(LocalDateTime.now()))) {
            passwordResetTokenRepository.delete(resetToken);
            return false;
        }
        return true;
    }
    
    private void setUserActive(String token)
    {
        User user = getUserByToken(token);
        user.setActive((short)1);
        userRepository.save(user);
    }

    public boolean validateEmailVerificationToken(String token) throws InvalidTokenException
    {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(InvalidTokenException::new);        
        if ((verificationToken.getExpiryDate().isBefore(LocalDateTime.now()))) {
            verificationTokenRepository.delete(verificationToken);
            return false;
        }        
        setUserActive(token);        
        verificationTokenRepository.delete(verificationToken);
        return true;
    }
    
    public User registerNewUserAccount(UserDto userDto) throws UserAlreadyExistException, InvalidRoleException, UnmatchedPasswordException {
        if (!userRepository.findByUsername(userDto.getUsername()).isEmpty()) {
            throw new UserAlreadyExistException();
        }
        if (!userRepository.findByEmail(userDto.getEmail()).isEmpty()) {
            throw new UserAlreadyExistException();
        }
        
        if(userDto.getPassword().equals(userDto.getMatchingPassword()))
        {
            User user = new User();
            user.setGivenName(userDto.getGivenName());
            user.setFamilyName(userDto.getFamilyName());
            user.setUsername(userDto.getUsername());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setUserRole(userRoleRepository.findByName("ROLE_USER")
                    .orElseThrow(InvalidRoleException::new));
            return userRepository.save(user);
        }
        else
        {
            throw new UnmatchedPasswordException();
        }
    }
    
    public User getUserByUsername(String username) throws InvalidUserException
    {
        return userRepository.findByUsername(username).orElseThrow(InvalidUserException::new);
    }

    public User getUserByToken(String verificationToken) throws InvalidTokenException
    {
        if(passwordResetTokenRepository.findByToken(verificationToken).isPresent())
        {
            return passwordResetTokenRepository.findByToken(verificationToken).get().getUser();
        }
        return verificationTokenRepository.findByToken(verificationToken).orElseThrow(InvalidTokenException::new).getUser();
    }
    
    public void createVerificationTokenForUser(String token, String username) throws InvalidUserException
    {        
        User user = userRepository.findByUsername(username).orElseThrow(InvalidUserException::new);
        
        verificationTokenRepository.findByToken(token).ifPresent(t -> verificationTokenRepository.delete(t));        
        verificationTokenRepository.save(new VerificationToken(token, user));        
    }
    
    public void createPasswordResetTokenForUser(String token, String username) throws InvalidUserException
    {        
        User user = userRepository.findByUsername(username).orElseThrow(InvalidUserException::new);
        
        passwordResetTokenRepository.findAllByUser(user).stream().forEach(t -> passwordResetTokenRepository.delete(t));
        passwordResetTokenRepository.save(new PasswordResetToken(token, user));        
    }

    public User changeUserPassword(PasswordDto passwordDto) throws UnmatchedPasswordException, InvalidCurrentPasswordException
    {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(passwordDto.getToken()).orElseThrow(InvalidTokenException::new);
        User user = passwordResetToken.getUser();
        if (passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword()))
        {
            if (passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword()))
            {
                user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
            }
            else
            {
                throw new UnmatchedPasswordException();
            }
        }
        else
        {
            throw new InvalidCurrentPasswordException();
        }
        
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
        return user;
    }
}
