package uk.gov.companieshouse.registers.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDeleteDelta;
import uk.gov.companieshouse.api.delta.RegistersDeleteDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.registers.consumer.apiclient.RegistersApiClient;
import uk.gov.companieshouse.registers.consumer.serdes.RegistersDeltaDeserialiser;

@Component
public class DeleteDeltaService implements DeltaService {

    private final RegistersDeltaDeserialiser deserialiser;
    private final RegistersApiClient apiClient;

    public DeleteDeltaService(RegistersDeltaDeserialiser deserialiser, RegistersApiClient apiClient) {
        this.deserialiser = deserialiser;
        this.apiClient = apiClient;
    }

    @Override
    public void process(ChsDelta delta) {
        RegistersDeleteDelta deleteDelta = deserialiser.deserialiseRegistersDeleteDelta(delta.getData());
        apiClient.deleteRegisters(deleteDelta.getCompanyNumber());
    }
}
