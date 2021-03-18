/**
 * 
 */
package com.smoothstack.utopia.userauthservice.web.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.smoothstack.utopia.userauthservice.validation.ValidEmail;
import com.smoothstack.utopia.userauthservice.validation.ValidPassword;
import com.smoothstack.utopia.userauthservice.validation.ValidUsername;

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
public class UserDTO {    
    @NotNull
    @ValidUsername
    @Size(min = 8, max = 32, message = "{Size.userDto.username}")
    private String username;
    
    @NotNull
    @Size(min = 10, max = 16, message = "{Size.userDto.phone}")
    private String phone;

    @NotNull
    @ValidEmail
    @Size(min = 6, max = 128, message = "{Size.userDto.email}")
    private String email;
    
    @NotNull
    @Size(min = 1, max = 32, message = "{Size.userDto.familyName}")
    private String familyName;
    
    @NotNull
    @Size(min = 1, max = 32, message = "{Size.userDto.givenName}")
    private String givenName;

    @NotNull
    @ValidPassword
    private String password;
    
    @NotNull
    @Size(min = 8, max = 32)
    private String matchingPassword;

    @NotNull
    @Size(min = 9, max = 16, message = "{Size.userDto.role}")
    private String role;
}
