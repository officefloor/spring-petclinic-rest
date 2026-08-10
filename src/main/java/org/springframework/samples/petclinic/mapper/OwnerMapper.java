package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    /** Fixed city-to-region table used to derive an owner's locality. */
    java.util.Map<String, String> CITY_REGION =
        java.util.Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range {@code {low, high}} keyed by region
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), used to derive an owner's
     * locality from the postcode in preference to the city.
     */
    java.util.Map<String, int[]> REGION_POSTCODE_RANGES =
        java.util.Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The canonical region derived for the owner, preferring the postcode. The
     * postcode is matched against each region's inclusive range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099) first; only when the postcode is absent or in
     * no known range does this fall back to the fixed city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD). Returns 'UNKNOWN' when neither
     * the postcode nor the city resolves to a region.
     */
    default String locality(Owner owner) {
        String byPostcode = regionForPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * The region whose inclusive postcode range contains {@code postcode}, or
     * {@code null} when the postcode is absent, not a 4-digit number, or in no
     * known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's numeric membership level, assigned on creation. It starts at 1, gains
     * 1 when an email address is present, and gains 1 when the owner has no namesakes
     * (namesakeCount is 0), capped at 3. Level 4 is reserved for tenure.
     */
    default Integer membershipLevel(Owner owner) {
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

    /**
     * The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY
     * is the last two digits of the registrationDate year (e.g. 'SYD-SMI-0007-M26').
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * A stable identifier for the owner's household, derived deterministically from the
     * normalized last name and address (trimmed, lower-cased, whitespace collapsed).
     * Every owner sharing a household therefore receives the same identifier: the first
     * eight upper-case hex characters of the SHA-256 of '&lt;lastName&gt;|&lt;address&gt;'.
     */
    default String householdId(Owner owner) {
        String key = normalizeHouseholdKey(owner.getLastName()) + "|"
            + normalizeHouseholdKey(owner.getAddress());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The owner's derived identity key: the single value onto which all duplicate
     * detection is consolidated, formatted
     * '&lt;normalizedTelephone&gt;|&lt;email or empty&gt;|&lt;householdId&gt;'.
     * The telephone and email are the owner's stored (already normalized) values;
     * a {@code null} email contributes the empty string. Two owners are duplicates
     * only when their whole identity keys are equal.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        return telephone + "|" + email + "|" + householdId(owner);
    }

    private static String normalizeHouseholdKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
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
