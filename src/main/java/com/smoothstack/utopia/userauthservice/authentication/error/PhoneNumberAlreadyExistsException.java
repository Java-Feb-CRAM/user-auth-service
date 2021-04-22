package com.smoothstack.utopia.userauthservice.authentication.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Phone Number Already Exists.")
public final class PhoneNumberAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}

