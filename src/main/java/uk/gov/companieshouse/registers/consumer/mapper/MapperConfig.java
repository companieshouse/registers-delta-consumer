package uk.gov.companieshouse.registers.consumer.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindService;

@Configuration
public class MapperConfig {

    @Bean
    public TransactionKindService transactionKindService(@Value("${transaction-id-salt}") String transactionIdSalt) {
        return new TransactionKindService(transactionIdSalt);
    }
}
