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

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            return OwnerDto.ContactPreferenceEnum.EMAIL;
        }
        return OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's locality (region) from its city using a fixed
     * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD).
     * Returns {@code UNKNOWN} when the city is absent or not in the table.
     */
    default String locality(Owner owner) {
        String city = owner.getCity();
        if (city == null) {
            return "UNKNOWN";
        }
        switch (city) {
            case "Sydney":
                return "NSW";
            case "Melbourne":
                return "VIC";
            case "Brisbane":
                return "QLD";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Returns the owner's numeric membership level, assigned on create: it starts at 1, gains 1 when
     * an email is present, gains 1 when {@code namesakeCount} is 0, and is capped at 3 (level 4 is
     * reserved for tenure).
     */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isEmpty()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Builds the owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;'
     * where YY is the last two digits of the registrationDate year. Returns {@code null}
     * when either source field is absent.
     */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M"
            + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
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
