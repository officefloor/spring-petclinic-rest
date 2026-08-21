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
    @Mapping(target = "membershipLevel",
        expression = "java(Math.min(3, 1 "
            + "+ (owner.getEmail() != null && !owner.getEmail().isEmpty() ? 1 : 0) "
            + "+ (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0 ? 1 : 0)))")
    @Mapping(target = "locality", expression = "java(deriveLocality(owner))")
    @Mapping(target = "checkDigit", expression = "java(luhnCheckDigit(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isEmpty() "
            + "? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL "
            + ": org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE)")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Derive the locality region for an owner from its region-and-hash customer code. The customer code
     * is {@code <REGION>-<HASH8>}, so the locality is the region segment before the first hyphen (the
     * same region the identity itself was built from: postcode range first, then the city-to-region
     * table, otherwise "UNKNOWN"). When no customer code is present, the locality is "UNKNOWN".
     */
    default String deriveLocality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash > 0) {
                return customerCode.substring(0, dash);
            }
        }
        return "UNKNOWN";
    }

    /**
     * Compute the single Luhn check digit (0-9) over the digits contained in the given value.
     * Non-digit characters (such as the hyphens in a customer code) are ignored. A null or
     * digit-free value yields a check digit of 0.
     */
    default Integer luhnCheckDigit(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
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
