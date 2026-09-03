package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.util.Localities.timezoneFor(org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode())))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.util.CheckDigits.luhn(owner.getCustomerCode()))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(owner.getPossibleDuplicateOf() != null)")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.util.TelephoneFormatter.toDisplay(owner.getTelephone()))")
    @Mapping(target = "selfLink",
        expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    OwnerDto toOwnerDto(Owner owner);

    /** Age band derived from birthDate against registrationDate: MINOR (under 18), ADULT (18-64) or SENIOR (65+); null when birthDate is absent. */
    default String ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
    }

    /** The single duplicate-detection key: SHA-256 hex of normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName). */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.util.IdentityKeys.of(owner);
    }

    default String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Membership points: +2 for an email, +1 when namesakeCount is 0, +2 for a household of 3 or more, +3 for tenure over 365 days. */
    default int membershipPoints(Owner owner) {
        return org.springframework.samples.petclinic.util.MembershipLevels.points(owner);
    }

    /** The owner's stored household-capped level when frozen at create, otherwise the level derived from points. */
    default Integer membershipLevel(Owner owner) {
        Integer stored = owner.getMembershipLevel();
        return stored != null ? stored : org.springframework.samples.petclinic.util.MembershipLevels.level(owner);
    }

    /** The 'FY<YY>' fiscal year of the (business-day-adjusted) registrationDate; null when the date is absent. */
    default String fiscalYear(Owner owner) {
        return owner.getRegistrationDate() == null ? null
            : org.springframework.samples.petclinic.util.FiscalYears.label(owner.getRegistrationDate());
    }

    /** Derive the '<customerCode>-M<YY>' membership number, where YY is the last two digits of the registrationDate fiscal year. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            org.springframework.samples.petclinic.util.FiscalYears.startYear(owner.getRegistrationDate()) % 100);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "registrationDate", expression = "java(effectiveRegistrationDate(ownerDto))")
    @Mapping(target = "address", expression = "java(org.springframework.samples.petclinic.util.AddressNormalizer.compose(ownerDto.getAddressLine1(), ownerDto.getAddressLine2(), ownerDto.getAddress()))")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** The supplied registration date, or today's date when absent, rolled forward off weekends to a business day. */
    default java.time.LocalDate effectiveRegistrationDate(OwnerFieldsDto ownerDto) {
        java.time.LocalDate supplied = ownerDto.getRegistrationDate();
        return org.springframework.samples.petclinic.util.BusinessDays.toBusinessDay(
            supplied != null ? supplied : java.time.LocalDate.now());
    }

    /** Normalize to E.164 so the create path stores, and duplicate detection compares, that form. */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
    }

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    default OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
