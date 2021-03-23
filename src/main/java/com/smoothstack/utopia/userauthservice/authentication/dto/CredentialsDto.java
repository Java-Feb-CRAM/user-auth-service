/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Craig Saunders
 *
 */
@Getter
@Setter
@ToString
public class CredentialsDto {

    @NotNull(message = "Username is required")
    @Size(min = 8, max = 32, message = "{Size.userDto.username}")
    @Pattern(regexp = "[a-zA-Z]+")
    private String username;

    @NotNull(message = "Password is required")
    @Size(min = 8, max = 32, message = "Password must be between 8 to 32 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!#$%^&*_+=~])[A-Za-z\\d@!#$%^&*_+=~]{8,32}$")
    private String password;
}