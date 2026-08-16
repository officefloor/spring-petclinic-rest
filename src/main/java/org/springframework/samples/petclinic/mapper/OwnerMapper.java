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
    @Mapping(target = "locality",
        expression = "java(deriveLocality(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId()))")
    @Mapping(target = "checkDigit",
        expression = "java(luhnCheckDigit(owner.getCustomerCode()))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Derives the owner's locality (region). Prefers the postcode: a four-digit postcode is
     * mapped by inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). When the postcode
     * is absent or in no known range, falls back to the city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), otherwise "UNKNOWN". Preferring the postcode
     * returns the same region for known cities but disambiguates cities that share a name.
     */
    default String deriveLocality(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("[0-9]{4}")) {
            int value = Integer.parseInt(postcode);
            if (value >= 2000 && value <= 2099) return "NSW";
            if (value >= 3000 && value <= 3099) return "VIC";
            if (value >= 4000 && value <= 4099) return "QLD";
        }
        String city = owner.getCity();
        if ("Sydney".equals(city)) return "NSW";
        if ("Melbourne".equals(city)) return "VIC";
        if ("Brisbane".equals(city)) return "QLD";
        return "UNKNOWN";
    }

    /**
     * Computes the single Luhn check digit (0-9) over the digits contained in the given
     * customerCode. Non-digit characters (the separators) are ignored; from the rightmost digit
     * leftwards every second digit is doubled (subtracting 9 when the result exceeds 9), and the
     * check digit is {@code (10 - (sum % 10)) % 10}. Returns 0 when the code is null.
     */
    default int luhnCheckDigit(String customerCode) {
        if (customerCode == null) {
            return 0;
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
