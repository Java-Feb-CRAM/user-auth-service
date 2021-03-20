/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.error;

public final class InvalidJsonRequestException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7082839605779336103L;

    public InvalidJsonRequestException() {
        super();
    }

    public InvalidJsonRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidJsonRequestException(final String message) {
        super(message);
    }

    public InvalidJsonRequestException(final Throwable cause) {
        super(cause);
    }

}