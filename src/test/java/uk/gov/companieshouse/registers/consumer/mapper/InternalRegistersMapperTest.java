package uk.gov.companieshouse.registers.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.delta.RegisterItem;
import uk.gov.companieshouse.api.delta.RegisterSection;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindService;
import uk.gov.companieshouse.api.registers.InternalData;
import uk.gov.companieshouse.api.registers.InternalRegisters;
import uk.gov.companieshouse.api.registers.RegisterListDirectors;
import uk.gov.companieshouse.api.registers.RegisterListLLPMembers;
import uk.gov.companieshouse.api.registers.RegisterListLLPUsualResidentialAddress;
import uk.gov.companieshouse.api.registers.RegisterListMembers;
import uk.gov.companieshouse.api.registers.RegisterListPersonsWithSignificantControl;
import uk.gov.companieshouse.api.registers.RegisterListSecretaries;
import uk.gov.companieshouse.api.registers.RegisterListUsualResidentialAddress;
import uk.gov.companieshouse.api.registers.RegisteredItems;
import uk.gov.companieshouse.api.registers.RegisteredItems.RegisterMovedToEnum;
import uk.gov.companieshouse.api.registers.Registers;
import uk.gov.companieshouse.registers.consumer.exception.InvalidPayloadException;

@ExtendWith(MockitoExtension.class)
class InternalRegistersMapperTest {

    private static final String CONTEXT_ID = "context_id";
    private static final String COMPANY_NUMBER = "12345678";

    private static final String DIRECTORS_LINK = "/company/%s/officers?register_view=true&register_type=directors"
            .formatted(COMPANY_NUMBER);
    private static final String SECRETARIES_LINK = "/company/%s/officers?register_view=true&register_type=secretaries"
            .formatted(COMPANY_NUMBER);
    private static final String PSC_LINK = "/company/%s/persons-with-significant-control?register_view=true"
            .formatted(COMPANY_NUMBER);

    private static final String TRANSACTION_ID = "9876543210";
    private static final String ENCODED_ID = "encoded_id";
    private static final String FILING_HISTORY_LINK = "/company/%s/filing-history/%s"
            .formatted(COMPANY_NUMBER, ENCODED_ID);

    private static final String ROA = "ROA";
    private static final String COMPANIES_HOUSE = "COMPANIES_HOUSE";
    private static final String SAIL = "SAIL";
    private static final String UNSPECIFIED = "UNSPECIFIED";


    @InjectMocks
    private InternalRegistersMapper mapper;
    @Mock
    private TransactionKindService transactionKindService;

    @Test
    void mapInternalRegistersDirectorsWithFilingHistoryLink() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .directors(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .transactionId(TRANSACTION_ID)
                                .chipsDescription(COMPANIES_HOUSE)
                                .movedOn("20231030111806"))));

        when(transactionKindService.encodeTransactionId(any())).thenReturn(ENCODED_ID);

        InternalRegisters expected = getExpected(new Registers()
                .directors(new RegisterListDirectors()
                        .registerType(RegisterListDirectors.RegisterTypeEnum.DIRECTORS)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.PUBLIC_REGISTER)
                                .links(Map.of("filing", FILING_HISTORY_LINK))))
                        .links(Map.of("directors_register", DIRECTORS_LINK))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verify(transactionKindService).encodeTransactionId(TRANSACTION_ID);
    }

    @Test
    void mapInternalRegistersDirectorsNoLinks() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .directors(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(SAIL)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .directors(new RegisterListDirectors()
                        .registerType(RegisterListDirectors.RegisterTypeEnum.DIRECTORS)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.SINGLE_ALTERNATIVE_INSPECTION_LOCATION)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersDirectorsSortedWithNoLink() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .directors(new RegisterSection()
                        .items(List.of(
                                new RegisterItem()
                                        .chipsDescription(COMPANIES_HOUSE)
                                        .movedOn("20231030111806"),
                                new RegisterItem()
                                        .chipsDescription(ROA)
                                        .movedOn("20240927111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .directors(new RegisterListDirectors()
                        .registerType(RegisterListDirectors.RegisterTypeEnum.DIRECTORS)
                        .items(List.of(
                                new RegisteredItems()
                                        .movedOn(LocalDate.parse("2024-09-27"))
                                        .registerMovedTo(RegisterMovedToEnum.REGISTERED_OFFICE),
                                new RegisteredItems()
                                        .movedOn(LocalDate.parse("2023-10-30"))
                                        .registerMovedTo(RegisterMovedToEnum.PUBLIC_REGISTER)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersLlpMembers() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .llpMembers(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(ROA)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .llpMembers(new RegisterListLLPMembers()
                        .registerType(RegisterListLLPMembers.RegisterTypeEnum.LLP_MEMBERS)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.REGISTERED_OFFICE)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersLlpUsualResidentialAddress() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .llpUsualResidentialAddress(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(SAIL)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .llpUsualResidentialAddress(new RegisterListLLPUsualResidentialAddress()
                        .registerType(
                                RegisterListLLPUsualResidentialAddress.RegisterTypeEnum.LLP_USUAL_RESIDENTIAL_ADDRESS)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.SINGLE_ALTERNATIVE_INSPECTION_LOCATION)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersMembers() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .members(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(UNSPECIFIED)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .members(new RegisterListMembers()
                        .registerType(RegisterListMembers.RegisterTypeEnum.MEMBERS)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.UNSPECIFIED_LOCATION)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersSecretaries() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .secretaries(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(COMPANIES_HOUSE)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .secretaries(new RegisterListSecretaries()
                        .registerType(RegisterListSecretaries.RegisterTypeEnum.SECRETARIES)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.PUBLIC_REGISTER)))
                        .links(Map.of("secretaries_register", SECRETARIES_LINK))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersPersonsWithSignificantControl() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .personsWithSignificantControl(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(COMPANIES_HOUSE)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .personsWithSignificantControl(new RegisterListPersonsWithSignificantControl()
                        .registerType(
                                RegisterListPersonsWithSignificantControl.RegisterTypeEnum.PERSONS_WITH_SIGNIFICANT_CONTROL)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.PUBLIC_REGISTER)))
                        .links(Map.of("persons_with_significant_control_register", PSC_LINK))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersPersonsWithSignificantControlNoLinks() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .personsWithSignificantControl(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(ROA)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .personsWithSignificantControl(new RegisterListPersonsWithSignificantControl()
                        .registerType(
                                RegisterListPersonsWithSignificantControl.RegisterTypeEnum.PERSONS_WITH_SIGNIFICANT_CONTROL)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.REGISTERED_OFFICE)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersUsualResidentialAddress() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .usualResidentialAddress(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .chipsDescription(ROA)
                                .movedOn("20231030111806"))));

        InternalRegisters expected = getExpected(new Registers()
                .usualResidentialAddress(new RegisterListUsualResidentialAddress()
                        .registerType(
                                RegisterListUsualResidentialAddress.RegisterTypeEnum.USUAL_RESIDENTIAL_ADDRESS)
                        .items(List.of(new RegisteredItems()
                                .movedOn(LocalDate.parse("2023-10-30"))
                                .registerMovedTo(RegisterMovedToEnum.REGISTERED_OFFICE)))));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersEmptyRegisters() {
        // given
        RegisterDelta delta = getBaseRegisterDelta();

        InternalRegisters expected = new InternalRegisters()
                .externalData(new Registers())
                .internalData(new InternalData()
                        .deltaAt(OffsetDateTime.parse("2023-10-30T11:18:06.428778Z"))
                        .updatedBy(CONTEXT_ID));

        // when
        InternalRegisters actual = mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersInvalidChipsDescription() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .directors(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .transactionId(TRANSACTION_ID)
                                .chipsDescription("invalid")
                                .movedOn("20231030111806"))));

        // when
        Executable actual = () -> mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        InvalidPayloadException exception = assertThrows(InvalidPayloadException.class, actual);
        assertEquals("Invalid CHIPS Description: [invalid]", exception.getMessage());
        verifyNoInteractions(transactionKindService);
    }

    @Test
    void mapInternalRegistersNullChipsDescription() {
        // given
        RegisterDelta delta = getBaseRegisterDelta()
                .directors(new RegisterSection()
                        .items(List.of(new RegisterItem()
                                .transactionId(TRANSACTION_ID)
                                .movedOn("20231030111806"))));

        // when
        Executable actual = () -> mapper.mapInternalRegisters(delta, CONTEXT_ID);

        // then
        InvalidPayloadException exception = assertThrows(InvalidPayloadException.class, actual);
        assertEquals("Invalid CHIPS Description: [null]", exception.getMessage());
        verifyNoInteractions(transactionKindService);
    }

    private static InternalRegisters getExpected(Registers registers) {
        return new InternalRegisters()
                .externalData(registers)
                .internalData(new InternalData()
                        .deltaAt(OffsetDateTime.parse("2023-10-30T11:18:06.428778Z"))
                        .updatedBy(CONTEXT_ID));
    }

    private static RegisterDelta getBaseRegisterDelta() {
        return new RegisterDelta()
                .companyNumber(COMPANY_NUMBER)
                .deltaAt("20231030111806428778");
    }
}