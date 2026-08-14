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
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Computes the owner's check digit: a single Luhn check digit (0-9) over the digits contained
     * in the owner's customer code. Returns {@code null} when the customer code is absent.
     */
    default Integer checkDigit(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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

    /**
     * Derives the owner's duplicate-detection identity key, formatted
     * {@code '<normalizedTelephone>|<email>|<householdId>'}. The email and household-id segments are
     * empty when the respective field is absent. The telephone and email are already stored in their
     * normalized (E.164 / lower-cased) form, so the stored values are used directly. This is the single
     * key against which owner duplicates are detected: two owners collide only when their whole keys
     * match.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Fixed city-to-region table used to derive an owner's locality.
     */
    java.util.Map<String, String> CITY_REGION =
        java.util.Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range {@code [low, high]} owned by each region: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099. Used to resolve an owner's region from the postcode first.
     */
    java.util.Map<String, int[]> REGION_POSTCODES =
        java.util.Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * Derives the owner's locality, the {@code REGION} segment of the owner's customer code (the
     * region-and-hash identity {@code '<REGION>-<HASH8>'}). Only when no customer code is present does
     * it fall back to deriving the region directly, preferring the postcode's {@link #REGION_POSTCODES}
     * range and then the fixed {@link #CITY_REGION} city table. Returns the canonical region string, or
     * {@code "UNKNOWN"} when neither source resolves a region.
     */
    default String locality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            String region = dash >= 0 ? customerCode.substring(0, dash) : customerCode;
            if (!region.isEmpty()) {
                return region;
            }
        }
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ex) {
                // Not a numeric postcode; fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Derives the owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present (non-blank), otherwise {@code "PHONE"}.
     */
    default String contactPreference(Owner owner) {
        String email = owner.getEmail();
        return email != null && !email.isBlank() ? "EMAIL" : "PHONE";
    }

    /**
     * Derives the owner's membership level, a number from 1 to 3 determined on creation. It starts
     * at {@code 1}, gains {@code 1} when an email is present, gains {@code 1} when the owner has no
     * namesakes ({@code namesakeCount} is {@code 0}), and is capped at {@code 3} (level {@code 4} is
     * reserved for tenure).
     */
    default Integer membershipLevel(Owner owner) {
        Integer namesakeCount = owner.getNamesakeCount();
        String email = owner.getEmail();
        boolean noNamesakes = namesakeCount != null && namesakeCount == 0;
        boolean hasEmail = email != null && !email.isBlank();
        int level = 1;
        if (hasEmail) {
            level++;
        }
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Builds the owner's membership number, formatted {@code '<customerCode>-M<YY>'} where
     * {@code YY} is the last two digits of the registration date's year (e.g. {@code "NSW-3F2A1B9C-M26"}).
     * Returns {@code null} when the customer code or registration date is absent.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int yy = owner.getRegistrationDate().getYear() % 100;
        return String.format("%s-M%02d", owner.getCustomerCode(), yy);
    }

    /**
     * Builds the owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    default String initials(Owner owner) {
        String first = owner.getFirstName();
        String last = owner.getLastName();
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isEmpty()) {
            sb.append(Character.toUpperCase(first.charAt(0))).append('.');
        }
        if (last != null && !last.isEmpty()) {
            sb.append(Character.toUpperCase(last.charAt(0))).append('.');
        }
        return sb.toString();
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
