package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerIdentity;
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

    @Mapping(target = "displayName", expression = "java(toDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    @Mapping(target = "contactPreference", expression = "java(toContactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(toIdentityKey(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's derived identityKey — the single value all duplicate detection is
     * expressed through. See {@link OwnerIdentity}.
     */
    default String toIdentityKey(Owner owner) {
        return OwnerIdentity.of(owner);
    }

    /**
     * The owner's locality, derived by the shared {@link CityRegions} table. The postcode is
     * preferred: the region whose range contains it wins (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), falling back to the city (Sydney->NSW, Melbourne->VIC, Brisbane->QLD)
     * when the postcode is absent or in no known range, and 'UNKNOWN' when neither resolves.
     */
    default String toLocality(Owner owner) {
        return CityRegions.localityOf(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email is present,
     * otherwise 'PHONE'.
     */
    default OwnerDto.ContactPreferenceEnum toContactPreference(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isBlank())
                ? OwnerDto.ContactPreferenceEnum.EMAIL
                : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /** The owner's name formatted as 'LastName, FirstName'. */
    default String toDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a trailing dot. */
    default String toInitials(Owner owner) {
        return owner.getFirstName().substring(0, 1).toUpperCase()
                + "." + owner.getLastName().substring(0, 1).toUpperCase() + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
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
