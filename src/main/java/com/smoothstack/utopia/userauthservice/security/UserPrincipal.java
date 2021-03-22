/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/**
 * @author Craig Saunders
 *
 */
@Getter
public class UserPrincipal implements UserDetails {
    
    @Serial
    private static final long serialVersionUID = 7490163486050209257L;
    private String username;
    private String password;
    private String userRole;
    
    public UserPrincipal(String username, String password, String userRole)
    {
        this.username = username;
        this.password = password;
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
