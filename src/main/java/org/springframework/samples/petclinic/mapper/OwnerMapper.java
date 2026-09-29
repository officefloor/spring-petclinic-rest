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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
            expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
            expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() "
                    + "? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
                    + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".TelephoneNormalizer.toDisplay(owner.getTelephone()))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".CustomerCode.regionOf(owner.getCustomerCode()))")
    @Mapping(target = "timezone",
            expression = "java(org.springframework.samples.petclinic.rest.function.common"
                    + ".Localities.timezoneOfRegion(org.springframework.samples.petclinic.rest.function"
                    + ".owner.CustomerCode.regionOf(owner.getCustomerCode())))")
    @Mapping(target = "checkDigit",
            expression = "java(org.springframework.samples.petclinic.rest.function.common"
                    + ".Luhn.checkDigit(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber",
            expression = "java(owner.getCustomerCode() + \"-M\" "
                    + "+ String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "identityKey",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".IdentityKey.ofOwner(owner))")
    @Mapping(target = "contactPreference",
            expression = "java(hasEmail(owner) ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints",
            expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
            expression = "java(membershipLevel(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Whether the owner has a usable email address. */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    /**
     * The owner's membership points. Starting from 0: add 2 when an email address is present, add 1
     * when the owner has no namesakes (namesakeCount is 0), add 2 for a household of 3 or more
     * members, and add 3 when the owner's tenure exceeds 365 days.
     *
     * <p>The household size is not derived here: it depends on other owners, so it is computed by
     * {@link org.springframework.samples.petclinic.rest.function.owner.OwnerHousehold} before the
     * owner reaches this mapper and read from the entity, keeping this a pure transformation.
     */
    default int membershipPoints(Owner owner) {
        int points = 0;
        if (hasEmail(owner)) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        long tenureDays = owner.getRegistrationDate() == null ? 0
                : ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now());
        if (tenureDays > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, mapped from {@link #membershipPoints(Owner)}: level 1 for
     * 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
     */
    default int membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
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
