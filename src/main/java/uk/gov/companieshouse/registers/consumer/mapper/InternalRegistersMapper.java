package uk.gov.companieshouse.registers.consumer.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.RegisterDelta;
import uk.gov.companieshouse.api.delta.RegisterItem;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindService;
import uk.gov.companieshouse.api.registers.*;
import uk.gov.companieshouse.registers.consumer.exception.InvalidPayloadException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class InternalRegistersMapper {

    private static final DateTimeFormatter DELTA_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS").withZone(ZoneId.of("Z"));
    private static final DateTimeFormatter MOVED_ON_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String CHIPS_DESCRIPTION_ROA = "ROA";
    private static final String CHIPS_DESCRIPTION_SAIL = "SAIL";
    private static final String CHIPS_DESCRIPTION_COMPANIES_HOUSE = "COMPANIES_HOUSE";
    private static final String CHIPS_DESCRIPTION_UNSPECIFIED = "UNSPECIFIED";

    private static final String REGISTER_TYPE_LINK_SUFFIX = "_register";
    private static final String OFFICER_REGISTER_PATTERN = "/company/%s/officers?register_view=true&register_type=%s";
    private static final String PSC_REGISTER_PATTERN = "/company/%s/persons-with-significant-control?register_view=true";

    private static final String FILING_HISTORY_LINK_NAME = "filing";
    private static final String FILING_HISTORY_LINK_PATTERN = "/company/%s/filing-history/%s";

    private final TransactionKindService transactionKindService;

    public InternalRegistersMapper(@Value("${transaction-id-salt}") String transactionIdSalt) {
        this.transactionKindService = new TransactionKindService(transactionIdSalt);
    }

    public InternalRegisters mapInternalRegisters(RegisterDelta delta, String updatedBy) {
        InternalData internalData = new InternalData()
                .deltaAt(mapDeltaAt(delta.getDeltaAt()))
                .updatedBy(updatedBy);

        Registers externalData = mapRegisters(delta);

        return new InternalRegisters()
                .internalData(internalData)
                .externalData(externalData);
    }

    private OffsetDateTime mapDeltaAt(String deltaAt) {
        return ZonedDateTime.parse(deltaAt, DELTA_AT_FORMATTER).toOffsetDateTime();
    }

    private Registers mapRegisters(RegisterDelta delta) {
        Registers registers = new Registers();

        if (delta.getDirectors() != null) {
            RegisterListDirectors register = new RegisterListDirectors();
            register.setRegisterType(RegisterListDirectors.RegisterTypeEnum.DIRECTORS);
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getDirectors().getItems()));
            register.setLinks(mapOfficerRegisterLinks(delta.getCompanyNumber(), register.getItems(),
                    RegisterListDirectors.RegisterTypeEnum.DIRECTORS.getValue()));
            registers.setDirectors(register);
        }

        if (delta.getSecretaries() != null) {
            RegisterListSecretaries register = new RegisterListSecretaries();
            register.setRegisterType(RegisterListSecretaries.RegisterTypeEnum.SECRETARIES);
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getSecretaries().getItems()));
            register.setLinks(mapOfficerRegisterLinks(delta.getCompanyNumber(), register.getItems(),
                    RegisterListSecretaries.RegisterTypeEnum.SECRETARIES.getValue()));
            registers.setSecretaries(register);
        }

        if (delta.getPersonsWithSignificantControl() != null) {
            RegisterListPersonsWithSignificantControl register = new RegisterListPersonsWithSignificantControl();
            register.setRegisterType(RegisterListPersonsWithSignificantControl.RegisterTypeEnum.PERSONS_WITH_SIGNIFICANT_CONTROL);
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getPersonsWithSignificantControl().getItems()));
            register.setLinks(mapPscRegisterLinks(delta.getCompanyNumber(), register.getItems()));
            registers.setPersonsWithSignificantControl(register);
        }

        if (delta.getMembers() != null) {
            RegisterListMembers register = new RegisterListMembers();
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getMembers().getItems()));
            register.setRegisterType(RegisterListMembers.RegisterTypeEnum.MEMBERS);
            registers.setMembers(register);
        }

        if (delta.getUsualResidentialAddress() != null) {
            RegisterListUsualResidentialAddress register = new RegisterListUsualResidentialAddress();
            register.setRegisterType(RegisterListUsualResidentialAddress.RegisterTypeEnum.USUAL_RESIDENTIAL_ADDRESS);
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getUsualResidentialAddress().getItems()));
            registers.setUsualResidentialAddress(register);
        }

        if (delta.getLlpMembers() != null) {
            RegisterListLLPMembers register = new RegisterListLLPMembers();
            register.setRegisterType(RegisterListLLPMembers.RegisterTypeEnum.LLP_MEMBERS);
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getLlpMembers().getItems()));
            registers.setLlpMembers(register);
        }

        if (delta.getLlpUsualResidentialAddress() != null) {
            RegisterListLLPUsualResidentialAddress register = new RegisterListLLPUsualResidentialAddress();
            register.setRegisterType(RegisterListLLPUsualResidentialAddress.RegisterTypeEnum.LLP_USUAL_RESIDENTIAL_ADDRESS);
            register.setItems(mapRegisterItems(delta.getCompanyNumber(), delta.getLlpUsualResidentialAddress().getItems()));
            registers.setLlpUsualResidentialAddress(register);
        }

        return registers;
    }

    private Object mapOfficerRegisterLinks(String companyNumber, List<RegisteredItems> items, String registerType) {
        // only create the register type link if the latest (first in sorted array) item is at CH
        if (RegisteredItems.RegisterMovedToEnum.PUBLIC_REGISTER.equals(items.getFirst().getRegisterMovedTo())) {
            Map<String, String> links = new HashMap<>();
            links.put(registerType + REGISTER_TYPE_LINK_SUFFIX, OFFICER_REGISTER_PATTERN.formatted(companyNumber, registerType));
            return links;
        }
        return null;
    }

    private Object mapPscRegisterLinks(String companyNumber, List<RegisteredItems> items) {
        // only create the register type link if the latest (first in sorted array) item is at CH
        if (RegisteredItems.RegisterMovedToEnum.PUBLIC_REGISTER.equals(items.getFirst().getRegisterMovedTo())) {
            String pscLinkKey = RegisterListPersonsWithSignificantControl.RegisterTypeEnum.PERSONS_WITH_SIGNIFICANT_CONTROL.getValue()
                    + REGISTER_TYPE_LINK_SUFFIX;
            pscLinkKey = pscLinkKey.replaceAll("-", "_");
            Map<String, String> links = new HashMap<>();
            links.put(pscLinkKey, PSC_REGISTER_PATTERN.formatted(companyNumber));
            return links;
        }
        return null;
    }

    private List<RegisteredItems> mapRegisterItems(String companyNumber, List<RegisterItem> items) {
        List<RegisteredItems> mappedItems = new ArrayList<RegisteredItems>();
        for (RegisterItem item : items) {
            RegisteredItems mappedItem = new RegisteredItems();
            mappedItem.setRegisterMovedTo(mapRegisterMovedTo(item.getChipsDescription()));
            mappedItem.setMovedOn(mapMovedOn(item));
//            mappedItem.setTransactionId(mapTransactionId(item)); transaction_id is mapped in perl code in chs-transform-api but is later removed in the old chs-registers-api and replaced with "links.filing"
            mappedItem.setLinks(mapRegisterItemLinks(companyNumber, item));
            mappedItems.add(mappedItem);
        }

        // sort mappedItems into reverse date order i.e. newest first
        mappedItems.sort(new Comparator<RegisteredItems>() {
            @Override
            public int compare(RegisteredItems item1, RegisteredItems item2) {
                return item2.getMovedOn().compareTo(item1.getMovedOn()); // compare item2 to item1 so the order is newest first
            }
        });

        return mappedItems;
    }

    private RegisteredItems.RegisterMovedToEnum mapRegisterMovedTo(String chipsDescription) {
        return switch (chipsDescription) {
            case CHIPS_DESCRIPTION_ROA -> RegisteredItems.RegisterMovedToEnum.REGISTERED_OFFICE;
            case CHIPS_DESCRIPTION_SAIL -> RegisteredItems.RegisterMovedToEnum.SINGLE_ALTERNATIVE_INSPECTION_LOCATION;
            case CHIPS_DESCRIPTION_COMPANIES_HOUSE -> RegisteredItems.RegisterMovedToEnum.PUBLIC_REGISTER;
            case CHIPS_DESCRIPTION_UNSPECIFIED -> RegisteredItems.RegisterMovedToEnum.UNSPECIFIED_LOCATION;
            default -> throw new InvalidPayloadException("Invalid CHIPS Description: [%s]".formatted(chipsDescription), null);
        };
    }

    private LocalDate mapMovedOn(RegisterItem item) {
        return LocalDate.parse(item.getMovedOn(), MOVED_ON_FORMATTER);
    }

    private Object mapRegisterItemLinks(String companyNumber, RegisterItem item) {
        if (StringUtils.isEmpty(item.getTransactionId())) {
            return null;
        }

        String encodedId = transactionKindService.encodeTransactionId(item.getTransactionId());
        Map<String, String> links = new HashMap<>();
        links.put(FILING_HISTORY_LINK_NAME, FILING_HISTORY_LINK_PATTERN.formatted(companyNumber, encodedId));
        return links;
    }
}