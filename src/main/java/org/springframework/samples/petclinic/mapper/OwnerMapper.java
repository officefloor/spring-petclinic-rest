package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.identityKey(owner))")
    @Mapping(target = "bulkSignupWarning",
            expression = "java(owner.getBulkSignupWarning() != null && owner.getBulkSignupWarning())")
    @Mapping(target = "possibleDuplicate",
            expression = "java(owner.getPossibleDuplicateOf() != null)")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The stored E.164 'telephone' formatted for humans: the country code, a space, then the
     *  national digits grouped in threes (e.g. '+61 412 345 678'). The raw 'telephone' stays
     *  E.164. Null when the stored value is not a recognised E.164 number. */
    default String telephoneDisplay(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.E164Telephone
                .toDisplayOrNull(owner.getTelephone());
    }

    /** Age band derived from birthDate relative to registrationDate: 'MINOR' when under
     *  18, 'ADULT' from 18 to 64, 'SENIOR' at 65 or over. Null when either date is absent. */
    default String ageBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /** 'EMAIL' when the owner has a non-blank email address, otherwise 'PHONE'. */
    default String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    /** Upper-cased first letters of firstName and lastName, dot-separated with a trailing dot. */
    default String initials(Owner owner) {
        return owner.getFirstName().substring(0, 1).toUpperCase()
                + "." + owner.getLastName().substring(0, 1).toUpperCase() + ".";
    }

    /** Membership number '<customerCode>-M<YY>', YY = last two digits of the
     *  registrationDate year. Null unless both source fields are present. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
                owner.getRegistrationDate().getYear() % 100);
    }

    /** A single Luhn check digit (0-9) computed over the digits contained in the
     *  owner's customerCode. Null when the customerCode is absent. */
    default Integer checkDigit(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /** Membership points: starts at 0, adds 2 when an email is present, adds 1 when
     *  namesakeCount is 0, adds 2 for a household of 3 or more, and adds 3 for tenure
     *  (more than 365 days between the registrationDate and today). */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
                && java.time.temporal.ChronoUnit.DAYS.between(
                        owner.getRegistrationDate(), java.time.LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /** Numeric membership level from 1 to 4, mapped from membershipPoints: level 1 for
     *  0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more. */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /** The owner's locality: the REGION component of the customerCode ('<REGION>-<HASH8>'),
     *  which is derived from the postcode at creation. 'UNKNOWN' when the customerCode is absent
     *  or carries no region prefix. Derived from the region-and-hash identity so it always agrees
     *  with the code, membership number and check digit. */
    default String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return "UNKNOWN";
        }
        int dash = code.indexOf('-');
        return dash <= 0 ? "UNKNOWN" : code.substring(0, dash);
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

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
