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
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "householdId", expression = "java(formatHouseholdId(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(formatMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(formatBulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(formatCheckDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(formatAgeBand(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(formatPossibleDuplicate(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Format an owner's name as 'LastName, FirstName' from the stored names. */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
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
     * Compute the owner's membership level, the derived numeric level based on the owner's namesake
     * count and whether an email is present
     * (see {@link Owner#membershipLevel(Integer, String)}).
     */
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Owner.membershipLevel(owner.getNamesakeCount(), owner.getEmail());
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

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
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
