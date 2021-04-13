package com.smoothstack.utopia.userauthservice.security;

import static java.lang.String.format;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.authentication.error.InvalidCredentialsException;
import com.smoothstack.utopia.userauthservice.authentication.error.UserAccountInactiveException;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Rob Maes Mar 19 2021
 */
@Component
public class JwtUtil {

  private static final String JWT_ISSUER = "utopia.smoothstack.com";
  private static final int ONE_WEEK_MILLISECONDS = 7 * 24 * 60 * 60 * 1000;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Value("#{${ut.jwt}['jwtSecret']}")
  private String jwtSecret;

  public String generateJwt(String username) {
    return Jwts
      .builder()
      .setSubject(format("%s", username))
      .setIssuedAt(new Date())
      .setIssuer(JWT_ISSUER)
      .setExpiration(
        new Date(System.currentTimeMillis() + ONE_WEEK_MILLISECONDS)
      )
      .signWith(SignatureAlgorithm.HS512, jwtSecret)
      .compact();
  }

  public String getAuthenticatedJwt(String username, String password)
    throws InvalidCredentialsException {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(InvalidCredentialsException::new);
    if (!user.isActive()) {
      throw new UserAccountInactiveException();
    }
    if (passwordEncoder.matches(password, user.getPassword())) {
      return generateJwt(username);
    } else {
      throw new InvalidCredentialsException();
    }
  }
}
