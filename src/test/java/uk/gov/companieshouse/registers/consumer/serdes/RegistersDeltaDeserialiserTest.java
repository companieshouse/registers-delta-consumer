package uk.gov.companieshouse.registers.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.delta.RegistersDeleteDelta;
import uk.gov.companieshouse.registers.consumer.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
class RegistersDeltaDeserialiserTest {

    public static final String REGISTERS_DELTA = "registers delta json string";
    public static final String REGISTERS_DELETE_DELTA = "registers delete delta json string";
    @InjectMocks
    private RegistersDeltaDeserialiser deserialiser;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RegisterDelta expectedDelta;
    @Mock
    private RegistersDeleteDelta expectedDeleteDelta;

    @Test
    void shouldDeserialiseRegistersDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(RegisterDelta.class))).thenReturn(expectedDelta);

        // when
        RegisterDelta actual = deserialiser.deserialiseRegistersDelta(REGISTERS_DELTA);

        // then
        assertEquals(expectedDelta, actual);
        verify(objectMapper).readValue(REGISTERS_DELTA, RegisterDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrown() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(RegisterDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialiseRegistersDelta(REGISTERS_DELTA);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise delta", actual.getMessage());
        verify(objectMapper).readValue(REGISTERS_DELTA, RegisterDelta.class);
    }

    @Test
    void shouldDeserialiseRegistersDeleteDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(RegistersDeleteDelta.class))).thenReturn(expectedDeleteDelta);

        // when
        RegistersDeleteDelta actual = deserialiser.deserialiseRegistersDeleteDelta(REGISTERS_DELETE_DELTA);

        // then
        assertEquals(expectedDeleteDelta, actual);
        verify(objectMapper).readValue(REGISTERS_DELETE_DELTA, RegistersDeleteDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrownFromDeleteDelta()
            throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(RegistersDeleteDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialiseRegistersDeleteDelta(REGISTERS_DELETE_DELTA);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise DELETE delta", actual.getMessage());
        verify(objectMapper).readValue(REGISTERS_DELETE_DELTA, RegistersDeleteDelta.class);
    }
}