package com.aeotrade.provider.oauth.sms;

import org.springframework.security.core.AuthenticationException;

public class MobileCodeException extends AuthenticationException {
    // ~ Constructors
    // ===================================================================================================

    /**
     * Constructs a <code>BadCodeException</code> with the specified message.
     *
     * @param msg the detail message
     */
    public MobileCodeException(String msg) {
        super(msg);
    }

    /**
     * Constructs a <code>BadCodeException</code> with the specified message and
     * root cause.
     *
     * @param msg the detail message
     * @param t root cause
     */
    public MobileCodeException(String msg, Throwable t) {
        super(msg, t);
    }

}
