/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST,
        reason = "Invalid Json format or request"
      )
public final class InvalidJsonRequestException extends RuntimeException { private static final long serialVersionUID = 1L; }