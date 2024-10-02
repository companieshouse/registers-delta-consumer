package uk.gov.companieshouse.registers.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.registers.consumer.apiclient.RegistersApiClient;
import uk.gov.companieshouse.registers.consumer.mapper.InternalRegistersMapper;
import uk.gov.companieshouse.registers.consumer.serdes.RegistersDeltaDeserialiser;

@Component
public class UpsertDeltaService implements DeltaService {

    private final RegistersDeltaDeserialiser deserialiser;
    private final InternalRegistersMapper mapper;
    private final RegistersApiClient apiClient;

    public UpsertDeltaService(RegistersDeltaDeserialiser deserialiser, InternalRegistersMapper mapper,
            RegistersApiClient apiClient) {
        this.deserialiser = deserialiser;
        this.mapper = mapper;
        this.apiClient = apiClient;
    }


    @Override
    public void process(ChsDelta delta) {
        RegisterDelta registerDelta = deserialiser.deserialiseRegistersDelta(delta.getData());
        InternalRegisters apiRequest = mapper.mapInternalRegisters(registerDelta, delta.getContextId());
        apiClient.upsertRegisters(registerDelta.getCompanyNumber(), apiRequest);
    }
}
