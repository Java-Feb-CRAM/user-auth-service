/**
 * 
 */
package com.smoothstack.utopia.userauthservice.web.dto;

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
public class PasswordDTO {

    @NotNull
    private  String token;
    
    @NotNull
    @Pattern(regexp = "^(?:(?=.*[a-z])(?:(?=.*[A-Z])(?=.*[\\d\\W])|(?=.*\\W)(?=.*\\d))|(?=.*\\W)(?=.*[A-Z])(?=.*\\d)).$")
    @Size(min = 8, max = 32, message = "Password must be between 8 to 32 characters")
    private String newPassword;

}