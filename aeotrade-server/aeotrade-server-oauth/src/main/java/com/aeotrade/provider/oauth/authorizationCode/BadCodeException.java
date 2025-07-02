package com.aeotrade.provider.oauth.authorizationCode;

import org.springframework.security.core.AuthenticationException;

public class BadCodeException extends AuthenticationException {
    // ~ Constructors
    // ===================================================================================================

    /**
     * Constructs a <code>BadCodeException</code> with the specified message.
     *
     * @param msg the detail message
     */
    public BadCodeException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>BadCodeException</code> with the specified message and
     * root cause.
     *
     * @param msg the detail message
     * @param t root cause
     */
    public BadCodeException(String msg, Throwable t) {
        super(msg, t);
    }

}
