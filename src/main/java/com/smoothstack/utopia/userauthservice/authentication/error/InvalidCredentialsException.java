/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Craig Saunders
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid username or password.")
public class InvalidCredentialsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
