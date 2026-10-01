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

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "salutation", expression = "java(formatSalutation(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "householdId", expression = "java(formatHouseholdId(owner))")
    @Mapping(target = "fiscalYear", expression = "java(formatFiscalYear(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipPoints", expression = "java(formatMembershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(formatMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "timezone", expression = "java(formatTimezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(formatBulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(formatCheckDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(formatPossibleDuplicate(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(formatTelephoneDisplay(owner))")
    @Mapping(target = "deleted", expression = "java(formatDeleted(owner))")
    @Mapping(target = "selfLink", expression = "java(formatSelfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Build the owner's self link, the canonical API path for the owner formatted
     * {@code '/api/owners/<id>'} using the owner's own id. Returns {@code null} when the owner has
     * no id yet, so the field is omitted.
     */
    default String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /** Format an owner's name as 'LastName, FirstName' from the stored names. */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Compose the owner's salutation from the optional title and the last name
     * (see {@link Owner#salutation(String, String)}).
     */
    default String formatSalutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.salutation(owner.getTitle(), owner.getLastName());
    }

    /**
     * Format an owner's initials as the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot (e.g. 'J.S.').
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Compute the owner's household id, the stable identifier shared by all owners in the same
     * household, i.e. with the same last name and postcode (see {@link Owner#householdId(String, String)}).
     */
    default String formatHouseholdId(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.householdId(owner.getLastName(), owner.getPostcode());
    }

    /**
     * Compute the owner's fiscal year, the 'FY&lt;YY&gt;' value derived from the
     * business-day-adjusted registration date (see {@link Owner#fiscalYear(java.time.LocalDate)}).
     */
    default String formatFiscalYear(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.fiscalYear(owner.getRegistrationDate());
    }

    /**
     * Compute the owner's membership number, the stable identifier derived from the owner's
     * customer code and registration date (see {@link Owner#membershipNumber(String, java.time.LocalDate)}).
     */
    default String formatMembershipNumber(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate());
    }

    /**
     * Compute the owner's membership points, the derived points score based on whether an email is
     * present, the owner's namesake count, the owner's household size and the owner's tenure since
     * registration (see {@link Owner#membershipPoints(Integer, String, Integer, java.time.LocalDate)}).
     */
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.membershipPoints(owner.getNamesakeCount(), owner.getEmail(),
            owner.getHouseholdCount(), owner.getRegistrationDate());
    }

    /**
     * Compute the owner's membership level, mapped from the owner's membership points
     * (see {@link Owner#membershipLevel(int)}).
     */
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.membershipLevel(formatMembershipPoints(owner));
    }

    /**
     * Compute the owner's locality, the canonical region taken from the {@code REGION} segment of the
     * owner's customer code (see {@link Owner#locality(String)}).
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.locality(owner.getCustomerCode());
    }

    /**
     * Compute the owner's timezone, the IANA timezone name derived from the owner's locality/region
     * via the fixed region-to-timezone table (see {@link Owner#timezone(String)}). Returns
     * {@code null} when the owner's region has no mapped timezone, so the field is omitted.
     */
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.timezone(owner.getCustomerCode());
    }

    /**
     * Resolve the owner's bulk-signup warning flag, treating a missing (null) stored value as
     * {@code false} so the response always carries an explicit boolean.
     */
    default Boolean formatBulkSignupWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Derive the owner's contact preference, {@code 'EMAIL'} when an email is present and
     * {@code 'PHONE'} otherwise (see {@link Owner#contactPreference(String)}).
     */
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.contactPreference(owner.getEmail());
    }

    /**
     * Compute the owner's identity key, the single derived value consolidating the telephone, email
     * and household duplicate checks (see
     * {@link Owner#identityKey(String, String, String, String)}).
     */
    default String formatIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName(), owner.getPostcode());
    }

    /**
     * Compute the owner's check digit, the single Luhn check digit over the digits of the owner's
     * customer code (see {@link Owner#checkDigit(String)}).
     */
    default Integer formatCheckDigit(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.checkDigit(owner.getCustomerCode());
    }

    /**
     * Compute the owner's age band, derived from the birth date against the registration date
     * (see {@link Owner#ageBand(java.time.LocalDate, java.time.LocalDate)}). Returns {@code null}
     * when no birth date is set, so the field is omitted from the response.
     */
    default String formatAgeBand(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.ageBand(owner.getBirthDate(), owner.getRegistrationDate());
    }

    /**
     * Resolve the owner's possible-duplicate flag, treating a missing (null) stored value as
     * {@code false} so the response always carries an explicit boolean. The matching owner id is
     * carried separately in {@code possibleDuplicateOf}, mapped by name.
     */
    default Boolean formatPossibleDuplicate(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    /**
     * Format the owner's stored E.164 telephone for human display, as the country code, a space and
     * the national digits grouped in threes (see {@link Owner#telephoneDisplay(String)}). The raw
     * {@code telephone} field keeps the unformatted E.164 value.
     */
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.telephoneDisplay(owner.getTelephone());
    }

    /**
     * Resolve the owner's soft-delete flag, treating a missing (null) stored value as {@code false}
     * so the response always carries an explicit boolean (see {@link Owner#isDeleted()}).
     */
    default Boolean formatDeleted(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.isDeleted();
    }

    @Mapping(target = "householdCount", ignore = true)
    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdCount", ignore = true)
    @Mapping(target = "telephone", qualifiedByName = "normalizeTelephone")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** Normalize a telephone to its canonical E.164 form, matching {@code TelephoneValidator}. */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return Owner.normalizeTelephone(telephone);
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
