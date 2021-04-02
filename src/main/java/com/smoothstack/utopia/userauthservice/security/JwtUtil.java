package com.smoothstack.utopia.userauthservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import static java.lang.String.format;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.userauthservice.authentication.error.InvalidCredentialsException;
import com.smoothstack.utopia.userauthservice.authentication.error.UserAccountInactiveException;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;

/**
 * @author Rob Maes Mar 19 2021
 */
@Component
public class JwtUtil implements Serializable {
	private final String jwtIssuer = "utopia.smoothstack.com";
	private final int ONE_WEEK_MILLISECONDS = 7 * 24 * 60 * 60 * 1000;

	@Serial
	private static final long serialVersionUID = -569378531925824570L;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Value("${UT_JWT_SECRET}")
	private String jwtSecret;

	public String generateJwt(String username) {
		return Jwts.builder().setSubject(format("%s", username)).setIssuedAt(new Date()).setIssuer(jwtIssuer)
				.setExpiration(new Date(System.currentTimeMillis() + ONE_WEEK_MILLISECONDS))
				.signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
	}

	public String getAuthenticatedJwt(String username, String password) throws InvalidCredentialsException {
		User user = userRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);
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
