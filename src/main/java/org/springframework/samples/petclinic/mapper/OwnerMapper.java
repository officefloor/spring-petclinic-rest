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
import org.springframework.samples.petclinic.rest.validation.E164Telephone;
import org.springframework.samples.petclinic.util.AddressNormalizer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipTier", expression = "java(formatMembershipTier(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(formatBulkSignupWarning(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Resolve an owner's bulk-signup warning flag, coalescing a {@code null} stored value
     * (e.g. for owners created before the flag existed) to {@code false} so the response
     * always carries an explicit boolean.
     */
    default Boolean formatBulkSignupWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Known city-to-region mappings. Keyed by the exact city name, the canonical region
     * string is returned for a listed city and {@code "UNKNOWN"} for anything else.
     */
    Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derive an owner's locality from its city using the fixed {@link #CITY_REGIONS}
     * city-to-region table, returning the canonical region string or {@code "UNKNOWN"}
     * when the city is {@code null} or not present in the table.
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return CITY_REGIONS.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Determine an owner's membership tier: {@code "SILVER"} when the owner has no namesakes
     * (its {@code namesakeCount} is {@code 0}) and an email address is present, otherwise
     * {@code "BRONZE"}. A null or absent namesake count is treated as not being zero, so the
     * tier only reaches SILVER for an explicitly unique owner with an email.
     */
    default String formatMembershipTier(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean uniqueName = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return uniqueName && hasEmail ? "SILVER" : "BRONZE";
    }

    /**
     * Format an owner's membership number as {@code "<customerCode>-M<YY>"}, where {@code YY}
     * is the last two digits of the registration date year (e.g. {@code "TOW-SMI-0007-M26"}).
     * Returns {@code null} when either the customer code or registration date is absent, since
     * the number cannot be derived without both.
     */
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Format an owner's name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Format an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalize a postal address for storage into its canonical form (see
     * {@link AddressNormalizer}), so it is persisted and returned the same way regardless of
     * the formatting supplied by the caller. {@code @Named} keeps this out of MapStruct's
     * automatic method selection, so it only runs where a mapping explicitly references it via
     * {@code qualifiedByName}.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalize a telephone number for storage into E.164 form (see {@link E164Telephone}).
     * The requirement that a valid E.164 number can be formed is enforced separately by
     * {@code TelephoneValidation}, so for already-validated input this returns the E.164
     * string; it is idempotent, so re-normalizing a stored value yields the same value.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164Telephone.normalize(telephone);
    }

    /**
     * Normalize an email address for storage by lower-casing it, so it is persisted and
     * returned in a canonical form regardless of the casing supplied by the caller.
     * Syntactic validity is enforced separately by the {@code @Email} constraint on the DTO.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase();
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
