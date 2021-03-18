/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException, UsernameNotFoundException {
        // Get authorization header and validate
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.equals("") || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Get jwt token and validate
        final String token = header.split(" ")[1].trim();
        if (!jwtTokenUtil.validate(token)) {
            chain.doFilter(request, response);
            return;
        }

        User user = userRepository.findByUsername(jwtTokenUtil.getUsername(token));
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username: " + jwtTokenUtil.getUsername(token));
        }
        
        UserPrincipal userDetails = new UserPrincipal(user, 
                user.getUserRole().getName());
        
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails.getAuthorities()
        );

        authentication
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}