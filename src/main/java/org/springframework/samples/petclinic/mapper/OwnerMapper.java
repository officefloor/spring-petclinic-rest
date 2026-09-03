package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.owner.MembershipLevel;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    @Autowired
    protected OwnerRepository ownerRepository;

    /** Membership points for the owner (see {@link MembershipLevel}). */
    protected int membershipPoints(Owner owner) {
        return MembershipLevel.points(owner, ownerRepository);
    }

    /** Membership level derived from the owner's membership points. */
    protected int membershipLevel(Owner owner) {
        return MembershipLevel.level(membershipPoints(owner));
    }

    @Mapping(target = "selfLink", expression = "java(owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(owner.getTitle() == null ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials", expression = "java(owner.getFirstName().substring(0, 1).toUpperCase() + \".\" + owner.getLastName().substring(0, 1).toUpperCase() + \".\")")
    @Mapping(target = "membershipNumber", expression = "java(owner.getCustomerCode() != null && owner.getRegistrationDate() != null ? owner.getCustomerCode() + \"-M\" + String.format(\"%02d\", org.springframework.samples.petclinic.rest.function.owner.FiscalYear.endYear(owner.getRegistrationDate()) % 100) : null)")
    @Mapping(target = "fiscalYear", expression = "java(owner.getRegistrationDate() == null ? null : org.springframework.samples.petclinic.rest.function.owner.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.rest.function.owner.Locality.locality(owner))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.rest.function.owner.Timezone.of(org.springframework.samples.petclinic.rest.function.owner.Locality.locality(owner)))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null && !owner.getEmail().isEmpty() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand", expression = "java(owner.getBirthDate() == null ? null : org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.rest.function.owner.CheckUniqueIdentity.identityKey(owner))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.rest.function.owner.CheckDigit.checkDigit(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay.display(owner.getTelephone()))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "registrationDate", expression = "java(ownerDto.getRegistrationDate() != null ? ownerDto.getRegistrationDate() : java.time.LocalDate.now())")
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
