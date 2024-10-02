package uk.gov.companieshouse.registers.consumer.apiclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.registers.request.PrivateRegistersDelete;
import uk.gov.companieshouse.api.handler.delta.registers.request.PrivateRegistersUpsert;
import uk.gov.companieshouse.api.handler.delta.registers.request.PrivateRegistersUpsertResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.registers.consumer.logging.DataMapHolder;

@ExtendWith(MockitoExtension.class)
class RegistersApiClientTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_URI = "/company/%s/registers".formatted(COMPANY_NUMBER);
    private static final String REQUEST_ID = "request_id";

    @InjectMocks
    private RegistersApiClient registersApiClient;

    @Mock
    private Supplier<InternalApiClient> internalApiClientFactory;
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private InternalRegisters requestBody;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateRegistersUpsertResourceHandler privateRegistersUpsertResourceHandler;
    @Mock
    private PrivateRegistersDelete privateRegistersDelete;
    @Mock
    private PrivateRegistersUpsert privateRegistersUpsert;

    @Test
    void shouldSendSuccessfulPutRequest() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putRegisters()).thenReturn(privateRegistersUpsertResourceHandler);
        when(privateRegistersUpsertResourceHandler.upsert(any(), any())).thenReturn(privateRegistersUpsert);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        registersApiClient.upsertRegisters(COMPANY_NUMBER, requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putRegisters();
        verify(privateRegistersUpsertResourceHandler).upsert(REQUEST_URI, requestBody);
        verify(privateRegistersUpsert).execute();
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putRegisters()).thenReturn(privateRegistersUpsertResourceHandler);
        when(privateRegistersUpsertResourceHandler.upsert(any(), any())).thenReturn(privateRegistersUpsert);
        when(privateRegistersUpsert.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        registersApiClient.upsertRegisters(COMPANY_NUMBER, requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putRegisters();
        verify(privateRegistersUpsertResourceHandler).upsert(REQUEST_URI, requestBody);
        verify(privateRegistersUpsert).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putRegisters()).thenReturn(privateRegistersUpsertResourceHandler);
        when(privateRegistersUpsertResourceHandler.upsert(any(), any())).thenReturn(privateRegistersUpsert);
        when(privateRegistersUpsert.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        registersApiClient.upsertRegisters(COMPANY_NUMBER, requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putRegisters();
        verify(privateRegistersUpsertResourceHandler).upsert(REQUEST_URI, requestBody);
        verify(privateRegistersUpsert).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldSendSuccessfulDeleteRequest() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteRegisters(anyString())).thenReturn(privateRegistersDelete);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        registersApiClient.deleteRegisters(COMPANY_NUMBER);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).deleteRegisters(REQUEST_URI);
        verify(privateRegistersDelete).execute();
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingDeleteRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteRegisters(anyString())).thenReturn(privateRegistersDelete);
        when(privateRegistersDelete.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        registersApiClient.deleteRegisters(COMPANY_NUMBER);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).deleteRegisters(REQUEST_URI);
        verify(privateRegistersDelete).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingDeleteRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteRegisters(anyString())).thenReturn(privateRegistersDelete);
        when(privateRegistersDelete.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        registersApiClient.deleteRegisters(COMPANY_NUMBER);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).deleteRegisters(REQUEST_URI);
        verify(privateRegistersDelete).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }
}
