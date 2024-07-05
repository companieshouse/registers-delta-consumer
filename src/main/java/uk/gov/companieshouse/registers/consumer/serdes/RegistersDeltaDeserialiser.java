package uk.gov.companieshouse.registers.consumer.serdes;

import static uk.gov.companieshouse.registers.consumer.Application.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDeleteDelta;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.delta.RegistersDeleteDelta;
import uk.gov.companieshouse.registers.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.registers.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class RegistersDeltaDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ObjectMapper objectMapper;

    RegistersDeltaDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RegisterDelta deserialiseRegistersDelta(String data) {
        try {
            return objectMapper.readValue(data, RegisterDelta.class);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Unable to deserialise delta: [%s]".formatted(data), ex, DataMapHolder.getLogMap());
            throw new NonRetryableException("Unable to deserialise delta", ex);
        }
    }

    public RegistersDeleteDelta deserialiseRegistersDeleteDelta(String data) {
        try {
            return objectMapper.readValue(data, RegistersDeleteDelta.class);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Unable to deserialise DELETE delta: [%s]".formatted(data), ex, DataMapHolder.getLogMap());
            throw new NonRetryableException("Unable to deserialise DELETE delta", ex);
        }
    }
}
