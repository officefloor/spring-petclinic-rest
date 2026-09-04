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
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.util.CustomerCode.region(owner.getCustomerCode()))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.util.LuhnCheckDigit.of(owner.getCustomerCode()))")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", owner.getRegistrationDate().getYear() % 100))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "contactPreference", expression = "java((owner.getEmail() != null && !owner.getEmail().isBlank()) ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.util.OwnerIdentity.identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.util.TelephoneNormalizer.toDisplay(owner.getTelephone()))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives the owner's {@code ageBand} from its {@code birthDate}, measured against its
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64 and
     * {@code SENIOR} at 65 or older. Returns {@code null} when no birth date is present.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate asOf = owner.getRegistrationDate();
        if (birthDate == null || asOf == null) {
            return null;
        }
        int years = java.time.Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (years < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Computes the owner's {@code membershipLevel}. Starts at 1; adds 1 when a non-blank email is
     * present; adds 1 when {@code namesakeCount} is 0; adds 1 when tenure exceeds 365 days (measured
     * from {@code registrationDate} to today). The result is capped at 4. Level 4 therefore requires
     * tenure of more than 365 days, so a newly created owner — whose tenure is zero — never exceeds
     * level 3.
     */
    default int membershipLevel(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null
                && java.time.temporal.ChronoUnit.DAYS.between(registrationDate, java.time.LocalDate.now()) > 365) {
            level++;
        }
        return Math.min(4, level);
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
