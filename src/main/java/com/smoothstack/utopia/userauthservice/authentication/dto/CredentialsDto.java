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

    @NotNull(message = "${message.username.null}")
    @Size(min = 8, max = 32, message = "${message.username.invalid.size}")
    @Pattern(regexp = "${regex.username}")
    private String username;

    @NotNull(message = "${message.password.null}")
    @Size(min = 8, max = 32, message = "${message.password.invalid.size}")
    @Pattern(regexp = "${regex.password}")
    private String password;
}