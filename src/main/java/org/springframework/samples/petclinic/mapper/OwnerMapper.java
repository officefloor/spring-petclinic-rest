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
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
                    + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(OwnerMapper.membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code identityKey}, the single key all duplicate detection is expressed
     * through: {@code '<normalizedTelephone>|<email or empty>|<householdId>'}
     * (see {@link org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity}).
     */
    default String identityKey(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity
                .identityKey(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * Derives the owner's preferred contact channel: {@code 'EMAIL'} when an email is present,
     * otherwise {@code 'PHONE'}.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Fixed city-to-region table backing the owner's {@code locality}: Sydney maps to {@code 'NSW'},
     * Melbourne to {@code 'VIC'} and Brisbane to {@code 'QLD'}.
     */
    java.util.Map<String, String> CITY_REGIONS = java.util.Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Derives the region from a 4-digit postcode: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
     * Returns {@code null} when the postcode is absent, non-numeric or in no known range, so the
     * caller can fall back to the city-to-region table.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int code;
        try {
            code = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        if (code >= 2000 && code <= 2099) {
            return "NSW";
        }
        if (code >= 3000 && code <= 3099) {
            return "VIC";
        }
        if (code >= 4000 && code <= 4099) {
            return "QLD";
        }
        return null;
    }

    /**
     * Derives the owner's locality (region), preferring the postcode range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099) and only falling back to the {@link #CITY_REGIONS} city table
     * when the postcode is absent or in no known range. Returns {@code 'UNKNOWN'} when neither
     * source resolves a region. This disambiguates cities that share a name.
     */
    default String locality(Owner owner) {
        String region = regionForPostcode(owner.getPostcode());
        if (region != null) {
            return region;
        }
        return CITY_REGIONS.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Derives the owner's membership level from 1 to 3: starting at 1, plus 1 when an email is
     * present, plus 1 when {@code namesakeCount} is 0, capped at 3 (level 4 is reserved for tenure).
     */
    static Integer membershipLevel(Owner owner) {
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
     * Derives the owner's membership number, formatted {@code '<customerCode>-M<YY>'} where YY is the
     * last two digits of the registrationDate year (e.g. {@code 'SYD-SMI-0007-M26'}). Returns {@code null}
     * when either source field is absent, so owners without a customerCode or registrationDate map cleanly.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Derives the owner's {@code checkDigit}: a single Luhn check digit (0-9) computed over the
     * digits contained in the {@code customerCode}. Returns {@code null} when the customerCode is
     * absent, so owners without a customerCode map cleanly.
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

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "householdMemberCount", ignore = true)
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
