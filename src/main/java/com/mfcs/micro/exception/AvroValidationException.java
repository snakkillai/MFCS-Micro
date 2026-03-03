package com.mfcs.micro.exception;

/**
 * Exception thrown when an Avro message fails schema validation.
 */
public class AvroValidationException extends RuntimeException {

    public AvroValidationException(String message) {
        super(message);
    }

    public AvroValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
