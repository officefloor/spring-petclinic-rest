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
    @Mapping(target = "initials",
            expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" "
                    + "+ owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "telephoneDisplay",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".TelephoneNormalizer.toDisplay(owner.getTelephone()))")
    @Mapping(target = "locality",
            expression = "java(org.springframework.samples.petclinic.rest.function.owner"
                    + ".CustomerCode.regionOf(owner.getCustomerCode()))")
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
    @Mapping(target = "membershipLevel",
            expression = "java(membershipLevel(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Whether the owner has a usable email address. */
    default boolean hasEmail(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }

    /**
     * The owner's numeric membership level. The pre-tenure factors start at 1, gain 1 for a present
     * email and 1 for having no namesakes (namesakeCount is 0), and are capped at 3. Level 4 is
     * reserved for tenure: it is awarded only when the owner's tenure exceeds 365 days. A newly
     * created owner has zero tenure, so it never exceeds level 3.
     */
    default int membershipLevel(Owner owner) {
        int base = Math.min(3, 1
                + (hasEmail(owner) ? 1 : 0)
                + (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0 ? 1 : 0));
        long tenureDays = owner.getRegistrationDate() == null ? 0
                : ChronoUnit.DAYS.between(owner.getRegistrationDate(), LocalDate.now());
        return base + (tenureDays > 365 ? 1 : 0);
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
