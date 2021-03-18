/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.smoothstack.utopia.shared.model.User;
/**
 * @author Craig Saunders
 *
 */
public class UserPrincipal implements UserDetails {
    
    /**
     * 
     */
    private static final long serialVersionUID = 7490163486050209257L;
    private User user;
    private String userRole;
    
    public UserPrincipal(User user, String userRole)
    {
        this.user = user;
        this.userRole = userRole;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        switch(userRole)
        {
        case "ROLE_ADMIN":
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        case "ROLE_AGENT":
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_AGENT"));
        default:
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }        
        return grantedAuthorities;
    }

    @Override
    public String getPassword()
    {
        return this.user.getPassword();
    }

    @Override
    public String getUsername()
    {
        return this.user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

}
