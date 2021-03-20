/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.dto;

import javax.validation.constraints.Email;
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
public class UserDto {    
    @NotNull
    @Pattern(regexp = "[a-zA-Z]+")
    @Size(min = 8, max = 32, message = "{Size.userDto.username}")
    private String username;
    
    @NotNull
    @Size(min = 10, max = 16, message = "{Size.userDto.phone}")
    private String phone;

    @NotNull
    @Email
    @Size(min = 6, max = 128, message = "{Size.userDto.email}")
    private String email;
    
    @NotNull
    @Size(min = 1, max = 32, message = "{Size.userDto.familyName}")
    private String familyName;
    
    @NotNull
    @Size(min = 1, max = 32, message = "{Size.userDto.givenName}")
    private String givenName;

    @NotNull
    @Pattern(regexp = "^(?:(?=.*[a-z])(?:(?=.*[A-Z])(?=.*[\\d\\W])|(?=.*\\W)(?=.*\\d))|(?=.*\\W)(?=.*[A-Z])(?=.*\\d)).$")
    @Size(min = 8, max = 32, message = "Password must be between 8 to 32 characters")
    private String password;
    
    @NotNull
    @Size(min = 8, max = 32)
    private String matchingPassword;

    @NotNull
    @Size(min = 9, max = 16, message = "{Size.userDto.role}")
    private String role;
}
