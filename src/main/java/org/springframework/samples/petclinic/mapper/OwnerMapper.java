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
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), \"\"))")
    @Mapping(target = "checkDigit", expression = "java(owner.getCustomerCode() == null ? null : org.springframework.samples.petclinic.rest.function.owner.CheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.rest.function.owner.CityLocality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    OwnerDto toOwnerDto(Owner owner);

    /** Membership level: start at 1, +1 when an email is present, +1 when the owner has no
     *  namesakes (namesakeCount 0), +1 when tenure exceeds 365 days. Level 4 requires that
     *  tenure, so a new owner (zero tenure) never exceeds 3. Capped at 4. */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        java.time.LocalDate registration = owner.getRegistrationDate();
        if (registration != null && registration.isBefore(java.time.LocalDate.now().minusDays(365))) {
            level++;
        }
        return Math.min(level, 4);
    }

    /** Membership number: '<customerCode>-M<YY>', YY being the last two digits of the
     *  registrationDate year. Null until both source fields are assigned. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /** Stable identifier for the household an owner belongs to: the first 12 hex characters of SHA-256
     *  over (normalizedLastName + '|' + postcode), so owners with the same last name and postcode
     *  share it. */
    default String householdId(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.HouseholdId.of(
                owner.getLastName(), owner.getPostcode());
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
