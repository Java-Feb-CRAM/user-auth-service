/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.dto;

import com.smoothstack.utopia.shared.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Craig Saunders
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class UserWithAccountVerificationTokenDto {
	private User user;
	private String accountVerificationToken;
}
