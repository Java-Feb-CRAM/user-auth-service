/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Craig Saunders
 *
 *         Passwords should have these criteria: - 8 to 32 characters - at least
 *         1 lowercase letter - at least 1 uppercase letter - at least 1 digit
 *         (0-9) - at least 1 special character (@!#$%^&*_+=~)
 *
 */
@Getter
@Setter
public class UserDto {
    @NotNull(message = "${message.username.null}")
    @Pattern(regexp = "${regex.username}")
    @Size(min = 8, max = 32, message = "${message.username.invalid.size}")
    private String username;

    @NotNull(message = "${message.phone.null}")
    @Size(min = 10, max = 16, message = "${message.phone.invalid.size}")
    private String phone;

    @NotNull(message = "${message.email.null}")
    @Email
    private String email;

    @NotNull(message = "${message.name.family.null}")
    @Size(min = 1, max = 32, message = "${message.name.family.invalid.size}")
    private String familyName;

    @NotNull(message = "${message.name.given.null}")
    @Size(min = 1, max = 32, message = "${message.name.given.invalid.size}")
    private String givenName;

    @NotNull(message = "${message.password.null}")
    @Pattern(regexp = "${regex.password}")
    @Size(min = 8, max = 32, message = "${message.password.invalid.size}")
    private String password;

    @NotNull(message = "${message.password.matching.null}")
    @Size(min = 8, max = 32, message = "${message.password.matching.invalid.size}")
    private String matchingPassword;
}
