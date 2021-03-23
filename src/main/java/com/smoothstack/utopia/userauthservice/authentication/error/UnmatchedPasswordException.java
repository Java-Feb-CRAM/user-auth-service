/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "New password and confirmation password do not match.")
public final class UnmatchedPasswordException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}