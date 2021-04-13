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
 */
@Getter
@Setter
public class CredentialsDto {

    @NotNull(message = "Username is required.")
    @Size(min = 8, max = 32, message = "Username must be between 8 and 32 characters.")
    @Pattern(regexp = "^[a-zA-Z]{1}[a-zA-Z\\d_]{7,31}$")
    private String username;

    @NotNull(message = "Password is required.")
    @Size(min = 8, max = 32, message = "Password must be between 8 to 32 characters.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*_+=~])[A-Za-z\\d@!#$%^&*_+=~]{8,32}$")
    private String password;
}