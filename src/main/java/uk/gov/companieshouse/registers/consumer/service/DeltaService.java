package uk.gov.companieshouse.registers.consumer.service;

import uk.gov.companieshouse.delta.ChsDelta;

public interface DeltaService {

    void process(ChsDelta delta);
}
