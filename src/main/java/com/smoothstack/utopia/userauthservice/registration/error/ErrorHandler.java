/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smoothstack.utopia.userauthservice.registration.dto.UserDto;

/**
 * @author Craig Saunders
 *
 */
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(UnmatchedPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UnmatchedPasswordException handleUnmatchedPasswordException(UnmatchedPasswordException e)
    {
        return e;
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleUserAlreadyExistException(UserAlreadyExistException e)
    {
        return e;
    }

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public InvalidRoleException handleInvalidRoleException(InvalidRoleException e)
    {
        return e;
    }

    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InvalidJsonRequestException handleJsonProcessingException(JsonProcessingException e)
    {
        return new InvalidJsonRequestException("Failed to process json request");
    }

    @ExceptionHandler(JsonMappingException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public InvalidJsonRequestException handleJsonMappingException(JsonMappingException e) throws JsonProcessingException
    {
        return new InvalidJsonRequestException((new ObjectMapper()).writeValueAsString(new UserDto()));
    }

    @ExceptionHandler(InvalidJsonRequestException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public InvalidJsonRequestException handleInvalidJsonRequestException(InvalidJsonRequestException e)
    {
        return new InvalidJsonRequestException(e);
    }
    
    @ExceptionHandler(NullTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public NullTokenException handleNullTokenException(NullTokenException e)
    {
        return e;
    }
}
