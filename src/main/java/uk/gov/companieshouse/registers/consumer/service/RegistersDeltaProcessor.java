package uk.gov.companieshouse.registers.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.registers.consumer.mapper.InternalRegistersMapper;

@Component
public class RegistersDeltaProcessor {
    private final InternalRegistersMapper internalRegistersMapper;

    public RegistersDeltaProcessor(InternalRegistersMapper internalRegistersMapper) {
        this.internalRegistersMapper = internalRegistersMapper;
    }

    public InternalRegisters processDelta(RegisterDelta delta, final String updatedBy) {
        return internalRegistersMapper.mapInternalRegisters(delta, updatedBy);
    }
}
