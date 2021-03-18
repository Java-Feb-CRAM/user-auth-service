/**
 * 
 */
package com.smoothstack.utopia.userauthservice.web.error;

public final class UnmatchedPasswordException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -2871103337707868918L;

    public UnmatchedPasswordException() {
        super();
    }

    public UnmatchedPasswordException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnmatchedPasswordException(final String message) {
        super(message);
    }

    public UnmatchedPasswordException(final Throwable cause) {
        super(cause);
    }

}