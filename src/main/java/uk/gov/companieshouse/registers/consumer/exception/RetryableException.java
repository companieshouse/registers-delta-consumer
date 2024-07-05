package uk.gov.companieshouse.registers.consumer.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
