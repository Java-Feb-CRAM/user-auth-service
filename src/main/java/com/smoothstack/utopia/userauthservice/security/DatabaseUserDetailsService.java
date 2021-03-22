/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.smoothstack.utopia.userauthservice.authentication.error.InvalidCredentialsException;
import com.smoothstack.utopia.userauthservice.authentication.error.UserAccountInactiveException;

import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;

/**
 * @author Craig Saunders
 *
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws InvalidCredentialsException, UserAccountInactiveException
    {
        User user = userRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new); 
        if (!user.isActive())
        {
            throw new UserAccountInactiveException();
        }
        return new UserPrincipal(user.getUsername(), user.getPassword(), user.getUserRole().getName());
    }
}