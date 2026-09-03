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
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.util.CustomerCodes.regionOf(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(org.springframework.samples.petclinic.util.CheckDigits.luhn(owner.getCustomerCode()))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(owner.getPossibleDuplicateOf() != null)")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.util.TelephoneFormatter.toDisplay(owner.getTelephone()))")
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

    /** The single duplicate-detection key: normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty). */
    default String identityKey(Owner owner) {
        return orEmpty(owner.getTelephone()) + '|' + orEmpty(owner.getEmail()) + '|' + orEmpty(owner.getHouseholdId());
    }

    default String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Membership points: +2 for an email, +1 when namesakeCount is 0, +2 for a household of 3 or more, +3 for tenure over 365 days. */
    default int membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > 365) {
            points += 3;
        }
        return points;
    }

    /** Numeric level derived from membershipPoints: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        return points <= 5 ? 3 : 4;
    }

    /** Days of tenure since registrationDate; 0 when the date is absent (a new owner has zero tenure). */
    default long tenureDays(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(registrationDate, java.time.LocalDate.now());
    }

    /** Derive the '<customerCode>-M<YY>' membership number, where YY is the last two digits of the registrationDate year. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "registrationDate", expression = "java(effectiveRegistrationDate(ownerDto))")
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
