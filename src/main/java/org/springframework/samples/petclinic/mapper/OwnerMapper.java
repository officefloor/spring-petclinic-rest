package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.CityRegion;
import org.springframework.samples.petclinic.util.ContactPreference;
import org.springframework.samples.petclinic.util.MembershipLevel;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's name formatted as 'LastName, FirstName'. */
    default String displayName(Owner owner) {
        return owner == null ? null : owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The canonical region derived from the owner's city (see {@link CityRegion#of(String)}). */
    default String locality(Owner owner) {
        return owner == null ? null : CityRegion.of(owner.getCity());
    }

    /** The upper-cased first letters of the first and last name, e.g. 'J.S.'. */
    default String initials(Owner owner) {
        return owner == null ? null
            : Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the
     * last two digits of the registration date's year, e.g. 'SYD-SMI-0007-M26'.
     */
    default String membershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * The owner's membership level (see {@link MembershipLevel}): a number from 1 to 3 that starts
     * at 1, gains 1 for a present email address and 1 for a unique name (namesakeCount is zero),
     * capped at 3.
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevel.of(owner);
    }

    /**
     * The owner's preferred contact channel (see {@link ContactPreference}): 'EMAIL' when an email
     * address is present, otherwise 'PHONE'.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        ContactPreference preference = ContactPreference.of(owner);
        return preference == null ? null : OwnerDto.ContactPreferenceEnum.valueOf(preference.name());
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
