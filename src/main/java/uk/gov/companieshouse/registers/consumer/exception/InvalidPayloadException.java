package uk.gov.companieshouse.registers.consumer.exception;

public class InvalidPayloadException extends RuntimeException {

    public InvalidPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
