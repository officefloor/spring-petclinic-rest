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
 * Maps Owner & OwnerDto using Mapstruct.
 *
 * <p>The {@code membershipLevel} is derived from the owner's fields together with the size of its
 * household (see {@link MembershipLevel}), so the mapper needs the {@link OwnerRepository}.
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    @Autowired
    protected OwnerRepository ownerRepository;

    /** Household-aware membership points, called from the {@code membershipPoints} mapping expression. */
    protected int membershipPoints(Owner owner) {
        return MembershipLevel.points(owner, this.ownerRepository);
    }

    /** Household-aware membership level, called from the {@code membershipLevel} mapping expression. */
    protected int membershipLevel(Owner owner) {
        return MembershipLevel.of(owner, this.ownerRepository);
    }

    /** Owner segment (tier + area), called from the {@code ownerSegment} mapping expression. */
    protected org.springframework.samples.petclinic.rest.dto.OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.OwnerSegment.of(owner, this.ownerRepository);
    }

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation",
        expression = "java(owner.getTitle() == null || owner.getTitle().isBlank() ? owner.getLastName() : owner.getTitle() + \" \" + owner.getLastName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneE164.display(owner.getTelephone()))")
    @Mapping(target = "membershipPoints",
        expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel",
        expression = "java(membershipLevel(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(ownerSegment(owner))")
    @Mapping(target = "locality",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.CityRegion.localityOfMemberId(owner.getMemberId(), owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.CityRegion.timezoneOfMemberId(owner.getMemberId(), owner.getCity(), owner.getPostcode()))")
    @Mapping(target = "contactPreference",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.ContactPreference.of(owner))")
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.of(owner))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner))")
    @Mapping(target = "fiscalYear",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.FiscalYear.labelOf(owner.getRegistrationDate()))")
    @Mapping(target = "selfLink",
        expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "capacityWarning", ignore = true)
    @Mapping(target = "bulkSignupWarning", ignore = true)
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "riskFlag", ignore = true)
    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
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
