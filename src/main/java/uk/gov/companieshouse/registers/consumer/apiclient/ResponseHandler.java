package uk.gov.companieshouse.registers.consumer.apiclient;

import static uk.gov.companieshouse.registers.consumer.Application.NAMESPACE;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.registers.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.registers.consumer.exception.RetryableException;
import uk.gov.companieshouse.registers.consumer.logging.DataMapHolder;

@Component
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String API_ERROR_RESPONSE_MESSAGE = "HTTP response code %d when calling registers API";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Failed call to registers API due to invalid URI";

    public void handle(ApiErrorResponseException ex) {

        final int statusCode = ex.getStatusCode();
        String message = API_ERROR_RESPONSE_MESSAGE.formatted(statusCode);

        if (HttpStatus.BAD_REQUEST.value() == ex.getStatusCode() || HttpStatus.CONFLICT.value() == ex.getStatusCode()) {
            LOGGER.error(message, ex, DataMapHolder.getLogMap());
            throw new NonRetryableException(message, ex);
        } else {
            LOGGER.info(String.format("%s. %s", message, Arrays.toString(ex.getStackTrace())),
                    DataMapHolder.getLogMap());
            throw new RetryableException(message, ex);
        }
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new NonRetryableException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }
}
