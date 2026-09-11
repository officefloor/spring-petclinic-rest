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

    @Mapping(target = "selfLink",
        expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.util.TelephoneNormalizer.formatForDisplay(owner.getTelephone()))")
    @Mapping(target = "membershipPoints",
        expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(membershipLevel(membershipPoints(owner)))")
    @Mapping(target = "locality",
        expression = "java(locality(owner))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.util.LocalityResolver.timezone(locality(owner)))")
    @Mapping(target = "contactPreference",
        expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) "
            + "? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "checkDigit",
        expression = "java(owner.getCustomerCode() == null ? null "
            + ": org.springframework.samples.petclinic.util.LuhnCheckDigit.compute(owner.getCustomerCode()))")
    @Mapping(target = "fiscalYear",
        expression = "java(owner.getRegistrationDate() == null ? null "
            + ": org.springframework.samples.petclinic.util.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "ageBand",
        expression = "java(owner.getBirthDate() == null || owner.getRegistrationDate() == null ? null "
            + ": (java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears() < 18 "
            + "? OwnerDto.AgeBandEnum.MINOR "
            + ": java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears() < 65 "
            + "? OwnerDto.AgeBandEnum.ADULT : OwnerDto.AgeBandEnum.SENIOR))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Returns the owner's canonical region (locality): the REGION segment of the customer code when
     * present, otherwise the region derived from the city and postcode, or {@code "UNKNOWN"}.
     */
    default String locality(Owner owner) {
        return owner.getCustomerCode() != null
            ? owner.getCustomerCode().substring(0, owner.getCustomerCode().indexOf('-'))
            : org.springframework.samples.petclinic.util.LocalityResolver.resolve(owner.getCity(), owner.getPostcode());
    }

    /**
     * Computes the owner's membership points: 0 to start, plus 2 when an email address is present,
     * plus 1 when the namesake count is 0, plus 2 for a household of 3 or more, and plus 3 for
     * tenure of at least one elapsed fiscal year (the fiscal year of today is later than the fiscal
     * year of the registration date).
     */
    default int membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
            && org.springframework.samples.petclinic.util.FiscalYear.elapsed(
                owner.getRegistrationDate(), java.time.LocalDate.now()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps membership points to a membership level: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4
     * for 6 or more.
     */
    default int membershipLevel(int membershipPoints) {
        if (membershipPoints <= 1) {
            return 1;
        }
        if (membershipPoints <= 3) {
            return 2;
        }
        if (membershipPoints <= 5) {
            return 3;
        }
        return 4;
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
