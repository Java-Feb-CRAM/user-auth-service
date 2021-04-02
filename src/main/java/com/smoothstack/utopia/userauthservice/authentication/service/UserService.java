/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smoothstack.utopia.shared.model.PasswordResetToken;
import com.smoothstack.utopia.shared.model.User;
import com.smoothstack.utopia.shared.model.VerificationToken;
import com.smoothstack.utopia.userauthservice.authentication.dto.CredentialsDto;
import com.smoothstack.utopia.userauthservice.authentication.dto.PasswordResetDto;
import com.smoothstack.utopia.userauthservice.authentication.dto.UserDto;
import com.smoothstack.utopia.userauthservice.authentication.error.InvalidCredentialsException;
import com.smoothstack.utopia.userauthservice.authentication.error.InvalidCurrentPasswordException;
import com.smoothstack.utopia.userauthservice.authentication.error.InvalidTokenException;
import com.smoothstack.utopia.userauthservice.authentication.error.PasswordResetTokenExpiredException;
import com.smoothstack.utopia.userauthservice.authentication.error.UnmatchedPasswordException;
import com.smoothstack.utopia.userauthservice.authentication.error.UserAlreadyExistException;
import com.smoothstack.utopia.userauthservice.authentication.error.VerificationTokenExpiredException;
import com.smoothstack.utopia.userauthservice.dao.PasswordResetTokenRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRepository;
import com.smoothstack.utopia.userauthservice.dao.UserRoleRepository;
import com.smoothstack.utopia.userauthservice.dao.VerificationTokenRepository;
import com.smoothstack.utopia.userauthservice.security.JwtUtil;

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
	@Autowired
	private JwtUtil jwtUtil;

	public boolean validatePasswordResetToken(String token) throws InvalidTokenException {

		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
				.orElseThrow(InvalidTokenException::new);
		if ((resetToken.getExpiryDate().isBefore(LocalDateTime.now()))) {
			passwordResetTokenRepository.delete(resetToken);
			return false;
		}
		return true;
	}

	private void setUserActive(String token) {
		User user = verificationTokenRepository.findByToken(token).orElseThrow(InvalidTokenException::new).getUser();
		user.setActive(true);
		userRepository.save(user);
	}

	public boolean validateUserAccountVerificationToken(String token) throws InvalidTokenException {
		VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
				.orElseThrow(InvalidTokenException::new);
		if ((verificationToken.getExpiryDate().isBefore(LocalDateTime.now()))) {
			verificationTokenRepository.delete(verificationToken);
			throw new VerificationTokenExpiredException();
		}

		if (verificationToken.getUser().isActive()) {
			verificationTokenRepository.delete(verificationToken);
			return false;
		}

		setUserActive(token);
		verificationTokenRepository.delete(verificationToken);
		return true;
	}

	public User registerNewUserAccount(UserDto userDto)
			throws UserAlreadyExistException, UnmatchedPasswordException {
		if (!userRepository.findByEmail(userDto.getEmail()).isEmpty()) {
			throw new UserAlreadyExistException();
		}
		if (!userRepository.findByUsernameIgnoreCase(userDto.getUsername()).isEmpty()) {
			throw new UserAlreadyExistException();
		}
		if (userDto.getPassword().equals(userDto.getMatchingPassword())) {
			User user = new User();
			user.setGivenName(userDto.getGivenName());
			user.setFamilyName(userDto.getFamilyName());
			user.setUsername(userDto.getUsername());
			user.setPassword(passwordEncoder.encode(userDto.getPassword()));
			user.setEmail(userDto.getEmail());
			user.setPhone(userDto.getPhone());
			user.setUserRole(userRoleRepository.findByName("ROLE_USER").get());
			return userRepository.save(user);
		} else {
			throw new UnmatchedPasswordException();
		}
	}

	public void createVerificationTokenForUser(String token, String username) throws InvalidCredentialsException {
		User user = userRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);

		verificationTokenRepository.findByUser(user).ifPresentOrElse(t -> verificationTokenRepository.delete(t),
				new Runnable() {
					@Override
					public void run() {
						verificationTokenRepository.save(new VerificationToken(token, user));
					}
				});
	}

	public void createPasswordResetTokenForUser(String token, String username) throws InvalidCredentialsException {
		User user = userRepository.findByUsername(username).orElseThrow(InvalidCredentialsException::new);

		passwordResetTokenRepository.findAllByUser(user).stream().forEach(t -> passwordResetTokenRepository.delete(t));
		passwordResetTokenRepository.save(new PasswordResetToken(token, user));
	}

	public void changeUserPassword(PasswordResetDto passwordDto)
			throws UnmatchedPasswordException, InvalidCurrentPasswordException, PasswordResetTokenExpiredException {		
		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(passwordDto.getToken())
				.orElseThrow(InvalidTokenException::new);
		if (passwordResetToken.getExpiryDate().isAfter(LocalDateTime.now())) {
			User user = passwordResetToken.getUser();
			if (passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
				if (passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword())) {
					user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
				} else {
					throw new UnmatchedPasswordException();
				}
			} else {
				throw new InvalidCurrentPasswordException();
			}

			userRepository.save(user);
			passwordResetTokenRepository.delete(passwordResetToken);
		} else {
			passwordResetTokenRepository.delete(passwordResetToken);
			throw new PasswordResetTokenExpiredException();
		}
	}

	public String authenticateUser(CredentialsDto credentialsDto) {
		return jwtUtil.getAuthenticatedJwt(credentialsDto.getUsername(), credentialsDto.getPassword());
	}

	public Map<String, String> getUserFromJwt(String postBody)
	{
		User user = userRepository.findByUsername(
				jwtUtil.getUsernameFromJwt(postBody.split(" ")[1].trim()))
				.orElseThrow(InvalidTokenException::new);
		Map<String, String> userMap = new HashMap<>();
		userMap.put("username", user.getUsername());
		userMap.put("email", user.getEmail());
		userMap.put("familyName", user.getFamilyName());
		userMap.put("givenName", user.getGivenName());
		userMap.put("phone", user.getPhone());
		userMap.put("role", user.getUserRole().getName());
		return userMap;
	}
}
