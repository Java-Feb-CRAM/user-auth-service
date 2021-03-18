/**
 * 
 */
package com.smoothstack.utopia.userauthservice.web.dto;

import javax.validation.constraints.NotNull;

import com.smoothstack.utopia.userauthservice.validation.ValidPassword;

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

    @ValidPassword
    private String newPassword;

}