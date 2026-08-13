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
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber",
        expression = "java(String.format(\"%s-M%02d\", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "locality",
        expression = "java(deriveLocality(owner))")
    @Mapping(target = "checkDigit",
        expression = "java(luhn(owner.getCustomerCode()))")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The single Luhn check digit (0-9) computed over the decimal digits contained in
     * {@code value}, right to left, doubling every second digit (starting with the
     * rightmost). Non-digit characters are ignored, so it works directly on a formatted
     * {@code customerCode} such as {@code 'SYD-SMI-0007'}.
     */
    default int luhn(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
     * Derives the canonical region for an owner, preferring the postcode.
     *
     * <p>The postcode range is consulted first (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099); only when the postcode is absent or in no known range does this
     * fall back to the fixed city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC,
     * Brisbane-&gt;QLD). Anything unresolved is {@code "UNKNOWN"}. Preferring the postcode
     * returns the same region for known cities while disambiguating shared city names.
     */
    default String deriveLocality(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("[0-9]{4}")) {
            int value = Integer.parseInt(postcode);
            if (value >= 2000 && value <= 2099) {
                return "NSW";
            }
            if (value >= 3000 && value <= 3099) {
                return "VIC";
            }
            if (value >= 4000 && value <= 4099) {
                return "QLD";
            }
        }
        String city = owner.getCity();
        if ("Sydney".equals(city)) {
            return "NSW";
        }
        if ("Melbourne".equals(city)) {
            return "VIC";
        }
        if ("Brisbane".equals(city)) {
            return "QLD";
        }
        return "UNKNOWN";
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
