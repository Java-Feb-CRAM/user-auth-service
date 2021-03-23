/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No token provided")
public final class InvalidTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}