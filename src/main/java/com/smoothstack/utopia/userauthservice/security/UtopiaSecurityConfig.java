/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Rob Maes Mar 19 2021
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class UtopiaSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private DatabaseUserDetailsService databaseUserDetailsService;
    @Autowired
    private JwtTokenFilter jwtRequestFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.userDetailsService(databaseUserDetailsService).passwordEncoder(encoder());
    }

    @Bean
    public PasswordEncoder encoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .authorizeRequests().antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/api/user/**").hasAnyRole("ADMIN", "AGENT", "USER")
            .antMatchers(HttpMethod.GET, "/password-reset/**").hasAnyRole("ADMIN", "AGENT", "USER")
            .antMatchers(HttpMethod.POST, "/password-reset/**").hasAnyRole("ADMIN", "AGENT", "USER")
            .antMatchers("/api/employee/**").hasAnyRole("ADMIN", "AGENT").antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/logout").permitAll().antMatchers(HttpMethod.POST, "/user/**").permitAll()
            .antMatchers(HttpMethod.GET, "/user/resendRegistrationToken").permitAll().antMatchers("/login")
            .permitAll().anyRequest().authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}