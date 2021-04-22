/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Username Already Exists.")
public final class UserAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
