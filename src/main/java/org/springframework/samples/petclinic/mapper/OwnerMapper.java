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

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(formatMembershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(formatContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(formatIdentityKey(owner))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives an owner's duplicate-detection identity key as
     * {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'} from the stored
     * (already E.164-normalized) telephone, lower-cased email and household id. A {@code null}
     * telephone, email or household id contributes the empty string in its position.
     */
    default String formatIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Derives an owner's preferred contact channel: {@code EMAIL} when an email address is present
     * (non-null and non-blank), otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives an owner's locality (region) from the city using a fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}). Returns
     * {@code "UNKNOWN"} when the city is not in the table.
     */
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return switch (owner.getCity() == null ? "" : owner.getCity()) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Computes an owner's membership level, a number from 1 to 3: starting at 1, plus 1 when an email
     * is present, plus 1 when the owner's namesake count is 0, capped at 3 (level 4 is reserved for
     * tenure).
     */
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (unique) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where YY is the last two
     * digits of the registration-date year (e.g. {@code "LON-SMI-0007-M26"}). Returns {@code null} when the
     * customer code or registration date is absent.
     */
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Formats an owner's display name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
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
