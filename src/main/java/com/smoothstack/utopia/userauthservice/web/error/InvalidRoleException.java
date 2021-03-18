/**
 * 
 */
package com.smoothstack.utopia.userauthservice.web.error;

public final class InvalidRoleException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7082839605779336103L;

    public InvalidRoleException() {
        super();
    }

    public InvalidRoleException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidRoleException(final String message) {
        super(message);
    }

    public InvalidRoleException(final Throwable cause) {
        super(cause);
    }

}