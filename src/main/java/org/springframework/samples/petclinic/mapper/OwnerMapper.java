package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.CityRegion;
import org.springframework.samples.petclinic.util.PostcodeRange;

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
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's name formatted as 'LastName, FirstName' from the stored names. */
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a
     *  trailing dot, e.g. 'J.S.'. */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /** The membership number '&lt;customerCode&gt;-M&lt;YY&gt;', where YY is the last two digits of
     *  the registrationDate year (e.g. 'LON-SMI-0007-M26'). Null when either source field is absent. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /** The owner's locality: the canonical region derived from its postcode range first,
     *  falling back to the fixed city-to-region table when the postcode is absent or in no
     *  known range, or 'UNKNOWN' when neither resolves a region. */
    default String locality(Owner owner) {
        String byPostcode = PostcodeRange.regionForPostcode(owner.getPostcode());
        return byPostcode != null ? byPostcode : CityRegion.locality(owner.getCity());
    }

    /** The owner's preferred contact method: 'EMAIL' when an email address is present,
     *  otherwise 'PHONE'. */
    default String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty() ? "EMAIL" : "PHONE";
    }

    /** The owner's derived duplicate-detection identity key: normalized telephone, email and
     *  household id joined by '|'. See {@link OwnerIdentity#key}. */
    default String identityKey(Owner owner) {
        return OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
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
