package uk.gov.companieshouse.registers.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.registers.consumer.apiclient.RegistersApiClient;
import uk.gov.companieshouse.registers.consumer.serdes.RegistersDeltaDeserialiser;

@Component
public class UpsertDeltaService implements DeltaService {

    private final RegistersDeltaDeserialiser deserialiser;
    private final RegistersDeltaProcessor deltaProcessor;
    private final RegistersApiClient apiClient;

    public UpsertDeltaService(RegistersDeltaDeserialiser deserialiser, RegistersDeltaProcessor deltaProcessor,
                              RegistersApiClient apiClient) {
        this.deserialiser = deserialiser;
        this.deltaProcessor = deltaProcessor;
        this.apiClient = apiClient;
    }

    @Override
    public void process(ChsDelta delta) {
        RegisterDelta registerDelta = deserialiser.deserialiseRegistersDelta(delta.getData());
        InternalRegisters apiRequest = deltaProcessor.processDelta(registerDelta, delta.getContextId());
        apiClient.upsertRegisters(registerDelta.getCompanyNumber(), apiRequest);
    }
}
