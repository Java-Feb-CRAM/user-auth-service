/**
 * 
 */
package com.smoothstack.utopia.userauthservice.registration.error;

public final class NullTokenException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 71103337707868918L;

    public NullTokenException() {
        super();
    }

    public NullTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NullTokenException(final String message) {
        super(message);
    }

    public NullTokenException(final Throwable cause) {
        super(cause);
    }

}