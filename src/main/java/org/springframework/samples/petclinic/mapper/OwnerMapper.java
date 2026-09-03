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
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(org.springframework.samples.petclinic.mapper.OwnerMapper.membershipLevel(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's derived {@code identityKey} = normalizedTelephone + '|' + (email or empty) + '|' +
     * householdId, the single key all duplicate detection is expressed through.
     */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(
                owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email is present, otherwise
     * {@code PHONE}.
     */
    default String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? "EMAIL" : "PHONE";
    }

    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /** City -> canonical region, from the fixed city-to-region table. */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    java.util.Map<String, int[]> REGION_RANGE = java.util.Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * The owner's locality (region), preferring the postcode: the region whose fixed range contains
     * the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). When the postcode is absent or in
     * no known range, falls back to the fixed city-to-region table (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD); {@code UNKNOWN} when neither yields a region.
     */
    default String locality(Owner owner) {
        String fromPostcode = regionFromPostcode(owner.getPostcode());
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /** The region whose fixed range contains {@code postcode}, or {@code null} when the postcode is
     *  absent, non-numeric, or in no known range. */
    private String regionFromPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (java.util.Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits of the owner's {@code customerCode}.
     * Null when the customer code is absent.
     */
    default Integer checkDigit(Owner owner) {
        if (owner.getCustomerCode() == null) {
            return null;
        }
        String code = owner.getCustomerCode();
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

    /**
     * The owner's membership number, formatted {@code <customerCode>-M<YY>} where YY is the last two
     * digits of the registrationDate year (e.g. {@code SYD-SMI-0007-M26}). Null when either the customer
     * code or the registration date is absent.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
                owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * The owner's membership level, a number from 1 to 3 assigned on creation. Starts at 1; add 1
     * when an email is present; add 1 when the owner's name was unique on creation (namesakeCount is
     * 0); capped at 3. Level 4 is reserved for tenure.
     */
    static int membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
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
