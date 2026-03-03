package com.mfcs.micro.exception;

/**
 * Exception thrown when the MFCS API call fails.
 */
public class MfcsApiException extends RuntimeException {

    private final int statusCode;

    public MfcsApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public MfcsApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
