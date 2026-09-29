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

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String firstName = owner.getFirstName() == null ? "" : owner.getFirstName();
        return lastName + ", " + firstName;
    }

    /**
     * Returns the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(Character.toUpperCase(firstName.charAt(0))).append('.');
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(Character.toUpperCase(lastName.charAt(0))).append('.');
        }
        return sb.toString();
    }

    /**
     * Formats the owner's membership number as {@code '<customerCode>-M<YY>'}: the customer
     * code, the literal {@code '-M'}, and the last two digits of the registrationDate year.
     * Returns {@code null} unless both the customer code and registration date are present.
     */
    default String membershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        return owner.getCustomerCode() + "-M" + yy;
    }

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
