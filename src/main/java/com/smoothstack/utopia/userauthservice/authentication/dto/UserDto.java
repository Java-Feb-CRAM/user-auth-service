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
    @NotNull(message = "Username is required.")
    @Pattern(regexp = "^[a-zA-Z]{1}[a-zA-Z\\d_]{7,31}$")
    @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters.")
    private String username;

    @NotNull(message = "Phone number is required.")
    @Size(min = 10, max = 16, message = "Phone number must be between 10 to 16 characters.")
    private String phone;

    @NotNull(message = "Email is required.")
    @Email
    private String email;

    @NotNull(message = "Family name is required.")
    @Size(min = 1, max = 32, message = "Family name must be between 1 and 32 characters.")
    private String familyName;

    @NotNull(message = "Given name is required.")
    @Size(min = 1, max = 32, message = "Given name must be between 1 and 32 characters.")
    private String givenName;

    @NotNull(message = "Password is required.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*_+=~])[A-Za-z\\d@!#$%^&*_+=~]{8,32}$")
    @Size(min = 8, max = 32, message = "Password must be between 8 to 32 characters.")
    private String password;

    @NotNull(message = "Matching password is required.")
    @Size(min = 8, max = 32, message = "Cannot match password with invalid size of characters.")
    private String matchingPassword;
}
