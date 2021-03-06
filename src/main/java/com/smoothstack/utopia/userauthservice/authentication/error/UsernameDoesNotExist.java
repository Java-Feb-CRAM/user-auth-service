/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Username Does Not Exist.")
public final class UsernameDoesNotExist extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
