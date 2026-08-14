package com.firstclub.membership.common.exception;

// Extending RuntimeException (not Exception) means callers aren't forced to
// catch or declare "throws" everywhere — it propagates up to
// GlobalExceptionHandler automatically, which is what actually turns it into
// an HTTP 404.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
