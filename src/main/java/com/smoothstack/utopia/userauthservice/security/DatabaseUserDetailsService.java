/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.UserRole;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;

/**
 * @author Craig Saunders
 *
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }        
        return new UserPrincipal(user, userRoleRepository.findById(user.getUserRole().getId()).orElse(new UserRole()).getName());
    }
}