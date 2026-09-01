package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "salutation",
        expression = "java(owner.getTitle() != null && !owner.getTitle().isBlank() ? owner.getTitle() + \" \" + owner.getLastName() : owner.getLastName())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay", expression = "java(TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "fiscalYear", expression = "java(MemberId.fiscalYear(owner.getMemberId()))")
    @Mapping(target = "locality", expression = "java(Locality.of(owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone", expression = "java(Locality.timezone(Locality.of(owner.getCity(), owner.getPostcode())))")
    @Mapping(target = "ownerSegment", expression = "java(OwnerSegment.of(membershipLevel(owner), MemberId.region(owner.getMemberId())))")
    OwnerDto toOwnerDto(Owner owner);

    /** Points: 0 base, +2 email present, +1 namesakeCount 0, +2 household of 3 or more,
     * +3 tenure over 365 days. */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (membershipTenured(owner)) {
            points += 3;
        }
        return points;
    }

    /** Level from points: 1 (0-1), 2 (2-3), 3 (4-5), 4 (6 or more). */
    default Integer membershipLevel(Owner owner) {
        return Math.min(membershipPoints(owner) / 2 + 1, 4);
    }

    /** Tenured once at least one fiscal year has elapsed since the registration date. */
    default boolean membershipTenured(Owner owner) {
        LocalDate registrationDate = owner.getRegistrationDate();
        return registrationDate != null
            && Fiscal.startYear(registrationDate) < Fiscal.startYear(LocalDate.now());
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
