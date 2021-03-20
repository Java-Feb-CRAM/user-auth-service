/**
 * 
 */
package com.smoothstack.utopia.userauthservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author Craig Saunders
 *
 */
@Configuration
@EnableWebSecurity
public class UtopiaJWTSecurityConfig extends WebSecurityConfigurerAdapter {   
    
    @Autowired
    private JwtTokenFilter jwtTokenFilter;    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {        
        http // use cors not csrf
                //.sessionManagement()
                //.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //.and()
                //.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .antMatchers("/api/user/**").hasAnyRole("ADMIN", "AGENT", "USER")
                    .antMatchers("/password-reset/**").hasAnyRole("ADMIN", "AGENT", "USER")
                    .antMatchers("/api/employee/**").hasAnyRole("ADMIN", "AGENT")
                    .antMatchers("/api/public/**").permitAll()
                    .antMatchers("/api/logout").permitAll()
                    .antMatchers(HttpMethod.POST, "/user/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/user/resendRegistrationToken").permitAll()
                    .antMatchers("/login").permitAll()
                    .anyRequest().authenticated()
                .and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
                    .logoutSuccessUrl("/login")
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true) 
                    .permitAll()
                .and()
                .formLogin()
                    //.loginPage("/api/login")
                    //.defaultSuccessUrl("/api/public")
                    .failureUrl("/login?error=true")
                    .permitAll();
    }
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    
    @Override @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }   
}

