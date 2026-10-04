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
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's preferred contact channel at read time: {@code EMAIL} when an email is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        return hasEmail(owner) ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /** Whether the owner has a non-blank email address. */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    /**
     * Reports the owner's bulk-signup warning: {@code true} when more than 80 owners had already
     * been created on this owner's registration business day at the time it was created. Treats a
     * missing value (owners created before this rule existed) as {@code false}.
     */
    default boolean bulkSignupWarning(Owner owner) {
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Derives the owner's numeric membership level from fields captured at creation: starts at 1,
     * adds 1 when an email is present, adds 1 when the owner had no namesakes at creation
     * ({@code namesakeCount == 0}), and is capped at 3. Level 4 is reserved for tenure.
     */
    default int membershipLevel(Owner owner) {
        int level = 1;
        if (hasEmail(owner)) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Derives the owner's locality (region) at read time, preferring the postcode range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) and falling back to the fixed
     * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) only when the postcode
     * is absent or in no known range. Returns the canonical region string, or {@code UNKNOWN}
     * when neither yields a known region.
     */
    default String locality(Owner owner) {
        String region = org.springframework.samples.petclinic.rest.function.owner.OwnerLocality
            .regionFor(owner.getPostcode(), owner.getCity());
        return region != null ? region : "UNKNOWN";
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
