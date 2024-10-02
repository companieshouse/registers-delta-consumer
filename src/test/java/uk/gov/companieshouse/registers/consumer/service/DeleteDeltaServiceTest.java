package uk.gov.companieshouse.registers.consumer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.RegistersDeleteDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.registers.consumer.apiclient.RegistersApiClient;
import uk.gov.companieshouse.registers.consumer.serdes.RegistersDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class DeleteDeltaServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String DELTA_DATA = "delta";

    @InjectMocks
    private DeleteDeltaService service;
    @Mock
    private RegistersDeltaDeserialiser deserialiser;
    @Mock
    private RegistersApiClient apiClient;
    @Mock
    private RegistersDeleteDelta delta;

    @Test
    void shouldSuccessfullyPassDeserialisedDeleteDeltaToApiClient() {
        // given
        when(deserialiser.deserialiseRegistersDeleteDelta(any())).thenReturn(delta);
        when(delta.getCompanyNumber()).thenReturn(COMPANY_NUMBER);

        ChsDelta chsDelta = new ChsDelta(DELTA_DATA, 0, "contextId", true);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseRegistersDeleteDelta(DELTA_DATA);
        verify(apiClient).deleteRegisters(COMPANY_NUMBER);
    }
}