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

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "toDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "toInitials")
    @Mapping(target = "membershipNumber", source = "owner", qualifiedByName = "toMembershipNumber")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "toMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "toLocality")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "toContactPreference")
    @Mapping(target = "identityKey", source = "owner", qualifiedByName = "toIdentityKey")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's duplicate-detection {@code identityKey}: the single key that consolidates
     * the former separate telephone, email and household checks. It is the owner's normalized
     * telephone, its email (lower-cased and trimmed, or the empty string when absent) and its
     * household identifier (or the empty string when absent), joined in that order by {@code '|'}.
     * Because the telephone is part of the key, two members of the same household with different
     * telephones have different identity keys; only an exact full-key match is a duplicate.
     */
    @Named("toIdentityKey")
    default String toIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone().trim();
        String email = owner.getEmail() == null ? "" : owner.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Derives the owner's preferred contact channel: {@code 'EMAIL'} when the owner has a non-blank
     * email, otherwise {@code 'PHONE'}.
     */
    @Named("toContactPreference")
    default String toContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Fixed city-to-region table used to derive an owner's locality.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derives the owner's locality as the canonical region for the owner's city from the fixed
     * city-to-region table ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}),
     * or {@code 'UNKNOWN'} when the city is not in the table.
     */
    @Named("toLocality")
    default String toLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Formats the owner's stored names as {@code 'LastName, FirstName'}.
     */
    @Named("toDisplayName")
    default String toDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Returns the owner's initials as the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    @Named("toInitials")
    default String toInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /**
     * Formats the owner's membership number as {@code '<customerCode>-M<YY>'}, where {@code YY} is
     * the last two digits of the {@code registrationDate} year, e.g. {@code 'SYD-SMI-0007-M26'}. Returns
     * {@code null} when either the customer code or the registration date is absent.
     */
    @Named("toMembershipNumber")
    default String toMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Returns the owner's numeric membership level, assigned on creation: starts at {@code 1}, plus
     * {@code 1} when a non-blank email is present, plus {@code 1} when the owner has no namesakes
     * ({@code namesakeCount} is 0), capped at {@code 3} (level 4 is reserved for tenure).
     */
    @Named("toMembershipLevel")
    default Integer toMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    private static String initial(String name) {
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
