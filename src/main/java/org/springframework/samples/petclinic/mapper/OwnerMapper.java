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
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber",
        expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality",
        expression = "java(locality(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(identityKey(owner))")
    @Mapping(target = "sharesHousehold", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derive the owner's identity key, the single value into which all duplicate detection is
     * consolidated: {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two
     * owners are duplicates only when their whole identity keys are equal. A null email or
     * householdId contributes an empty segment.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }

    /**
     * Determine the owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Build the owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is
     * the last two digits of the registrationDate year. Returns null when either source field is
     * absent so owners without an assigned code or registration date serialize cleanly.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
            owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Determine the owner's numeric membership level, assigned at creation. Starts at 1; add 1 when
     * an email address is present; add 1 when the owner has no namesakes (namesakeCount is 0);
     * capped at 3 (level 4 is reserved for tenure).
     */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
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
     * The fixed city-to-region table used to derive an owner's locality when the postcode is absent
     * or falls in no known range.
     */
    java.util.Map<String, String> CITY_REGION = java.util.Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -&gt; inclusive 4-digit postcode range {low, high}: NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099.
     */
    java.util.Map<String, int[]> REGION_POSTCODES = java.util.Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Derive the owner's locality (canonical region), preferring the postcode: look up the region by
     * postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only fall back to the
     * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD) when the postcode is
     * absent or in no known range. Returns 'UNKNOWN' when neither source resolves a region.
     */
    default String locality(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("\\d{4}")) {
            int value = Integer.parseInt(postcode);
            for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
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
