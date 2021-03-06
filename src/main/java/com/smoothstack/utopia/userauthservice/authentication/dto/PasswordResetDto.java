/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.dto;

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
public class PasswordResetDto {

    @NotNull
    private String token;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*_+=~])[A-Za-z\\d@!#$%^&*_+=~]{8,32}$")
    @Size(min = 8, max = 32)
    private String currentPassword;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*_+=~])[A-Za-z\\d@!#$%^&*_+=~]{8,32}$")
    @Size(min = 8, max = 32, message = "New password must be between 8 to 32 characters.")
    private String newPassword;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*_+=~])[A-Za-z\\d@!#$%^&*_+=~]{8,32}$")
    @Size(min = 8, max = 32)
    private String confirmNewPassword;

}