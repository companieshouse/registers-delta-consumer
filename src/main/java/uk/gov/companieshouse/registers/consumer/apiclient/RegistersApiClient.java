package uk.gov.companieshouse.registers.consumer.apiclient;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.registers.consumer.logging.DataMapHolder;

@Component
public class RegistersApiClient {

    private static final String REQUEST_URI = "/company/%s/registers";

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final ResponseHandler responseHandler;

    public RegistersApiClient(Supplier<InternalApiClient> internalApiClientFactory, ResponseHandler responseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
    }

    public void upsertRegisters(String companyNumber, InternalRegisters requestBody) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String formattedUri = REQUEST_URI.formatted(companyNumber);

        try {
            client.privateDeltaResourceHandler()
                    .putRegisters()
                    .upsert(formattedUri, requestBody)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }

    public void deleteRegisters(String companyNumber) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String formattedUri = REQUEST_URI.formatted(companyNumber);

        try {
            client.privateDeltaResourceHandler()
                    .deleteRegisters(formattedUri)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }

}
