package uk.gov.companieshouse.registers.consumer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.registers.consumer.apiclient.RegistersApiClient;
import uk.gov.companieshouse.registers.consumer.mapper.InternalRegistersMapper;
import uk.gov.companieshouse.registers.consumer.serdes.RegistersDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class UpsertDeltaServiceTest {

    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private UpsertDeltaService service;
    @Mock
    private RegistersDeltaDeserialiser deserialiser;
    @Mock
    private InternalRegistersMapper mapper;
    @Mock
    private RegistersApiClient apiClient;

    @Mock
    private RegisterDelta delta;
    @Mock
    private InternalRegisters apiRequest;

    @Test
    void shouldSuccessfullyPassDeserialisedAndMappedDeltaToApiClient() {
        // given
        when(deserialiser.deserialiseRegistersDelta(any())).thenReturn(delta);
        when(delta.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(mapper.mapInternalRegisters(any(), anyString())).thenReturn(apiRequest);

        ChsDelta chsDelta = new ChsDelta("delta", 0, "contextId", false);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseRegistersDelta("delta");
        verify(mapper).mapInternalRegisters(delta, "contextId");
        verify(apiClient).upsertRegisters(COMPANY_NUMBER, apiRequest);
    }
}